/* global __dirname */

const express = require('express');
const path = require('path');

const port = process.env.PORT || 3000;
const buildDir = path.join(__dirname, '/dist');

const app = express();
app.use(express.static(buildDir));

app.get('*', (req, res) => {
  res.sendFile(path.resolve(buildDir, 'index.html'));
});

app.listen(port);
console.log('Server Started');
