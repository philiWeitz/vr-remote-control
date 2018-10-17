import React from 'react';
import ReactDOM from 'react-dom';
import WebRTC from './WebRTC';


class IndexComponent extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      stream: null,
      renderWebRTC: true,
    };

    this.getMedia();
  }


  getMedia = () => {
    navigator.mediaDevices.getUserMedia({ audio: false, video: true })
      .catch((error) => {
        return navigator.mediaDevices.enumerateDevices()
          .then((devices) => {
            const cam = devices.find((device) => {
              return device.kind === 'videoinput';
            });
            const mic = devices.find((device) => {
              return device.kind === 'audioinput';
            });
            const constraints = { video: cam, audio: mic };
            return navigator.mediaDevices.getUserMedia(constraints);
          });
      })
      .then((stream) => {
        this.setState({ stream });
      });
  };


  renderContent = () => {
    const { stream, renderWebRTC } = this.state;

    if (stream && renderWebRTC) {
      return <WebRTC stream={stream}/>
    }
    return <div>Please allow access to your camera</div>
  };


  render() {
    return (
      <div>
        <h1>Web RTC</h1>
        {this.renderContent()}
      </div>
    )
  }
}


ReactDOM.render(<IndexComponent />, document.getElementById('index'));