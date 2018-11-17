import React from 'react';
import ReactDOM from 'react-dom';
import WebRTC from './web-rtc';


// default resolution for Raspberry PI camera is 352 x 288
// example URL parameters: roomId=123456789test&width=352&height=288

class IndexComponent extends React.Component {

  constructor(props) {
    super(props);

    const { roomId, width, height } = this.getUrlParameters();

    this.state = {
      roomId: roomId  || '987654321xxxx2',
      width: parseInt(width || '0', 10),
      height: parseInt(height || '0', 10),
      stream: null,
      renderWebRTC: true,
    };

    this.getMedia();
  }


  getUrlParameters = () => {
    const splitParams = location.search.split('&');

    return splitParams.reduce((result, paramString) => {
      const keyValuePair = paramString.replace('?','').split('=');
      if (keyValuePair.length === 2) {
        result[keyValuePair[0]] = keyValuePair[1];
      }
      return result;
    }, {});
  };


  getMedia = () => {
    const { width, height } = this.state;

    const videoParams = (width > 0 && height > 0)
      ? { width: { exact: width }, height: { exact: height }, frameRate: { ideal: 24, max: 30 }}
      : true;

    navigator.mediaDevices.getUserMedia({ audio: false, video: videoParams })
      .catch((error) => {
        return navigator.mediaDevices.enumerateDevices()
          .then((devices) => {

            const cam = devices.find((device) => {
              return device.kind === 'videoinput';
            });

            const mic = devices.find((device) => {
              return device.kind === 'audioinput';
            });

            const constraints = {
              audio: mic,
              video: { ...cam, ...videoParams },
            };

            return navigator.mediaDevices.getUserMedia(constraints);
          });
      })
      .then((stream) => {
        this.setState({ stream });
      });
  };


  renderResolution = () => {
    const { width, height } = this.state;

    if (width > 0 && height > 0) {
      return (
        <div>
          Camera resolution: width={width}, height={height}
        </div>
      );
    }
    return <div>No resolution specified, default is used</div>
  };


  renderContent = () => {
    const { stream, renderWebRTC, roomId } = this.state;

    if (stream && renderWebRTC) {
      return <WebRTC stream={stream} roomId={roomId} />
    }
    return <div>Please allow access to your camera</div>
  };


  render() {
    return (
      <div>
        <h1>Web RTC</h1>
        {this.renderResolution()}
        {this.renderContent()}
      </div>
    )
  }
}


ReactDOM.render(<IndexComponent />, document.getElementById('index'));