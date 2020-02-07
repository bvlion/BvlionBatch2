const functions = require('firebase-functions')
const express = require('express')
const basicAuth = require('basic-auth-connect')

const app = express()

app.all('/*', basicAuth((user, password) => user === "username_replace" && password === "password_replace"));

app.use(express.static(__dirname + '/static/'))

exports.app = functions.https.onRequest(app)
