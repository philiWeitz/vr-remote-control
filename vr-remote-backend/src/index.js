import React from 'react';
import ReactDOM from 'react-dom';
import WebRTC from './WebRTC';

const getUserMedia = require('getusermedia');


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
    getUserMedia({ video: true, audio: true }, (err, stream) => {
      if (err) return console.error(err);

      this.setState({ stream });
    })
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