package remote.vr.com.remote_android.main;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.webrtc.VideoRenderer;

import java.nio.ByteBuffer;

import remote.vr.com.remote_android.util.PerformanceLogger;

public class YuvFrame
{
    private static final String TAG = "VR-REMOTE";

    public int width;
    public int height;
    public int[] yuvStrides;
    public byte[] yPlane;
    public byte[] uPlane;
    public byte[] vPlane;
    public int rotationDegree;
    public long timestamp;

    private final Object planeLock = new Object();

    public static final int PROCESSING_NONE = 0x00;

    // Constants for indexing I420Frame information, for readability.
    private static final int I420_Y = 0;
    private static final int I420_V = 1;
    private static final int I420_U = 2;

    private static PerformanceLogger perfLogger =
            new PerformanceLogger(1000, "Performance YUV to ARGB");

    /**
     * Creates a YuvFrame from the provided I420Frame. Does no processing, and uses the current time as a timestamp.
     * @param i420Frame Source I420Frame.
     */
    @SuppressWarnings("unused")
    public YuvFrame( final VideoRenderer.I420Frame i420Frame )
    {
        fromI420Frame( i420Frame, PROCESSING_NONE, System.nanoTime() );
    }


    /**
     * Creates a YuvFrame from the provided I420Frame. Does any processing indicated, and uses the current time as a timestamp.
     * @param i420Frame Source I420Frame.
     * @param processingFlags Processing flags, YuvFrame.PROCESSING_NONE for no processing.
     */
    @SuppressWarnings("unused")
    public YuvFrame( final VideoRenderer.I420Frame i420Frame, final int processingFlags )
    {
        fromI420Frame( i420Frame, processingFlags, System.nanoTime() );
    }


    /**
     * Creates a YuvFrame from the provided I420Frame. Does any processing indicated, and uses the given timestamp.
     * @param i420Frame Source I420Frame.
     * @param processingFlags Processing flags, YuvFrame.PROCESSING_NONE for no processing.
     * @param timestamp The timestamp to give the frame.
     */
    public YuvFrame( final VideoRenderer.I420Frame i420Frame, final int processingFlags, final long timestamp )
    {
        fromI420Frame( i420Frame, processingFlags, timestamp );
    }


    /**
     * Replaces the data in this YuvFrame with the data from the provided frame. Will create new byte arrays to hold pixel data if necessary,
     * or will reuse existing arrays if they're already the correct size.
     * @param i420Frame Source I420Frame.
     * @param processingFlags Processing flags, YuvFrame.PROCESSING_NONE for no processing.
     * @param timestamp The timestamp to give the frame.
     */
    public void fromI420Frame( final VideoRenderer.I420Frame i420Frame, final int processingFlags, final long timestamp )
    {
        synchronized ( planeLock )
        {
            try
            {
                // Save timestamp
                this.timestamp = timestamp;

                // TODO: Check to see if i420Frame.yuvFrame is actually true?  Need to find out what the alternative would be.

                // Copy YUV stride information
                // TODO: There is probably a case where strides makes a difference, so far we haven't run across it.
                yuvStrides = new int[i420Frame.yuvStrides.length];
                System.arraycopy( i420Frame.yuvStrides, 0, yuvStrides, 0, i420Frame.yuvStrides.length );

                // Copy rotation information
                rotationDegree = i420Frame.rotationDegree;  // Just save rotation info for now, doing actual rotation can wait until per-pixel processing.

                copyPlanes( i420Frame );
            }
            catch ( Throwable t )
            {
                dispose();
            }
        }
    }


    public void dispose()
    {
        yPlane = null;
        vPlane = null;
        uPlane = null;
    }


    public boolean hasData()
    {
        return yPlane != null && vPlane != null && uPlane != null;
    }


    /**
     * Copy the Y, V, and U planes from the source I420Frame.
     * Sets width and height.
     * @param i420Frame Source frame.
     */
    private void copyPlanes( final VideoRenderer.I420Frame i420Frame )
    {
        synchronized ( planeLock )
        {
            // Copy the Y, V, and U ButeBuffers to their corresponding byte arrays.
            // Existing byte arrays are passed in for possible reuse.
            yPlane = copyByteBuffer( yPlane, i420Frame.yuvPlanes[I420_Y] );
            vPlane = copyByteBuffer( vPlane, i420Frame.yuvPlanes[I420_V] );
            uPlane = copyByteBuffer( uPlane, i420Frame.yuvPlanes[I420_U] );

            // Set the width and height of the frame.
            width = i420Frame.width;
            height = i420Frame.height;
        }
    }


    /**
     * Copies the entire contents of a ByteBuffer into a byte array.
     * If the byte array exists, and is the correct size, it will be reused.
     * If the byte array is null, or isn't properly sized, a new byte array will be created.
     * @param dst A byte array to copy the ByteBuffer contents to. Can be null.
     * @param src A ByteBuffer to copy data from.
     * @return A byte array containing the contents of the ByteBuffer. If the provided dst was non-null and the correct size,
     *         it will be returned. If not, a new byte array will be created.
     */
    private byte[] copyByteBuffer( @Nullable byte[] dst, @NonNull final ByteBuffer src )
    {
        // Create a new byte array if necessary.
        byte[] out;
        if ( ( null == dst ) || ( dst.length != src.capacity() ) )
        {
            out = new byte[ src.capacity() ];
        }
        else
        {
            out = dst;
        }

        // Copy the ByteBuffer's contents to the byte array.
        src.get( out );

        return out;
    }


    /**
     * Converts this YUV frame to an ARGB_8888 Bitmap. Applies stored rotation.
     * Remaning code based on http://stackoverflow.com/a/12702836 by rics (http://stackoverflow.com/users/21047/rics)
     * @return A new Bitmap containing the converted frame.
     */
    public Bitmap getBitmap()
    {
        // Calculate the size of the frame
        final int size = width * height;

        // Allocate an array to hold the ARGB pixel data
        final int[] argb = new int[size];

        convertYuvToArgbRot0( argb );

        // Create Bitmap from ARGB pixel data.
        return Bitmap.createBitmap( argb, width, height, Bitmap.Config.ARGB_8888 );
    }


    private void convertYuvToArgbRot0( final int[] outputArgb )
    {
        synchronized ( planeLock )
        {
            perfLogger.start();

            // Each U/V cell is overlaid on a 2x2 block of Y cells.
            // Loop through the size of the U/V planes, and manage the 2x2 Y block on each iteration.
            int u, v;
            int y1, y2, y3, y4;
            int p1, p2, p3, p4;
            int rowOffset = 0;  // Y and RGB array position is offset by an extra row width each iteration, since they're handled as 2x2 sections.

            final int size = width * height;
            final int uvSize = size / 4;
            final int uvWidth = width / 2;  // U/V plane width is half the width of the frame.

            for ( int i = 0; i < uvSize; i++ )
            {
                // At the end of each row, increment the Y/RGB row offset by an extra frame width
                if ( i != 0 && ( i % ( uvWidth ) ) == 0 )
                {
                    rowOffset += width;
                }

                // Calculate the 2x2 grid indices
                p1 = rowOffset + ( i * 2 );
                p2 = p1 + 1;
                p3 = p1 + width;
                p4 = p3 + 1;

                // Get the U and V values from the source.
                u = uPlane[i] & 0xff;
                v = vPlane[i] & 0xff;
                u = u - 128;
                v = v - 128;

                // Get the Y values for the matching 2x2 pixel block
                y1 = yPlane[p1] & 0xff;
                y2 = yPlane[p2] & 0xff;
                y3 = yPlane[p3] & 0xff;
                y4 = yPlane[p4] & 0xff;

                // Convert each YUV pixel to RGB
                outputArgb[p1] = convertYuvToArgb( y1, u, v );
                outputArgb[p2] = convertYuvToArgb( y2, u, v );
                outputArgb[p3] = convertYuvToArgb( y3, u, v );
                outputArgb[p4] = convertYuvToArgb( y4, u, v );
            }

            perfLogger.stop();
        }
    }


    private int convertYuvToArgb( final int y, final int u, final int v )
    {
        int r, g, b;

        // Convert YUV to RGB
        r = y + (int)(1.402f*v);
        g = y - (int)(0.344f*u +0.714f*v);
        b = y + (int)(1.772f*u);

        // Clamp RGB values to [0,255]
        r = ( r > 255 ) ? 255 : ( r < 0 ) ? 0 : r;
        g = ( g > 255 ) ? 255 : ( g < 0 ) ? 0 : g;
        b = ( b > 255 ) ? 255 : ( b < 0 ) ? 0 : b;

        // Shift the RGB values into position in the final ARGB pixel
        return 0xff000000 | (b<<16) | (g<<8) | r;
    }


    private class ArgbConversionRunnable implements Runnable {

        private final int mStartIndex;
        private final int mStopIndex;
        private final int[] mOutputArgb;
        private final int mFrameWidth;

        public ArgbConversionRunnable(final int[] outputArgb, int startIndex,
                                      int stopIndex, int frameWidth) {
            this.mStartIndex = startIndex;
            this.mStopIndex = stopIndex;

            this.mOutputArgb = outputArgb;
            this.mFrameWidth = frameWidth;
        }

        @Override
        public void run() {

            // Each U/V cell is overlaid on a 2x2 block of Y cells.
            // Loop through the size of the U/V planes, and manage the 2x2 Y block on each iteration.
            int u, v;
            int y1, y2, y3, y4;
            int p1, p2, p3, p4;

            final int uvWidth = mFrameWidth / 2;  // U/V plane width is half the width of the frame.

            int rowOffset = mStartIndex == 0 ? 0 : (mOutputArgb.length / 2);

            for ( int i = mStartIndex; i < mStopIndex; i++ )
            {
                // At the end of each row, increment the Y/RGB row offset by an extra frame width
                if ( i != 0 && ( i % ( uvWidth ) ) == 0 )
                {
                    rowOffset += mFrameWidth;
                }

                // Calculate the 2x2 grid indices
                p1 = rowOffset + ( i * 2 );
                p2 = p1 + 1;
                p3 = p1 + mFrameWidth;
                p4 = p3 + 1;

                // Get the U and V values from the source.
                u = uPlane[i] & 0xff;
                v = vPlane[i] & 0xff;
                u = u - 128;
                v = v - 128;

                // Get the Y values for the matching 2x2 pixel block
                y1 = yPlane[p1] & 0xff;
                y2 = yPlane[p2] & 0xff;
                y3 = yPlane[p3] & 0xff;
                y4 = yPlane[p4] & 0xff;

                // Convert each YUV pixel to RGB
                mOutputArgb[p1] = convertYuvToArgb( y1, u, v );
                mOutputArgb[p2] = convertYuvToArgb( y2, u, v );
                mOutputArgb[p3] = convertYuvToArgb( y3, u, v );
                mOutputArgb[p4] = convertYuvToArgb( y4, u, v );
            }
        }
    }
}