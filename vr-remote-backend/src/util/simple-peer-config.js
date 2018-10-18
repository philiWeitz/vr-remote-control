// found on: https://gist.github.com/sagivo/3a4b2f2c7ac6e1b5267c2f1f59ac6c6b

const config = {
  iceServers: [
    { urls: 'stun:stun.hosteurope.de:3478' },
    // { urls: 'stun:stun.l.google.com:19302' },
    // { urls: 'stun:stun1.l.google.com:19302'},
    // { urls: 'stun:stun2.l.google.com:19302'},
    // { urls: 'stun:stun3.l.google.com:19302'},
    // { urls: 'stun:stun4.l.google.com:19302'},

    {
      url: 'turn:numb.viagenie.ca',
      credential: 'muazkh',
      username: 'webrtc@live.com'
    },
  ]
};

export default config;
