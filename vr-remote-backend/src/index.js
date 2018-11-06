import React from 'react';
import ReactDOM from 'react-dom';
import WebRTC from './web-rtc';


const videoParams = true; // { width: 1280, height: 720, frameRate: 40 };


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