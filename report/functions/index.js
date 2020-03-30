const functions = require('firebase-functions')
const express = require('express')
const basicAuth = require('basic-auth-connect')
const admin = require('firebase-admin')
admin.initializeApp(functions.config().firebase)

const app = express()
app.all('/*', basicAuth((user, password) => user === "username_replace" && password === "password_replace"))
app.use(express.static(__dirname + '/static/'))

const db = admin.database()

app.use('/push_message/token', (req, res) => {
  db.ref('/fcm/token').once('value', (snapshot) => {
    const values = snapshot.val()

    const data = {
      title: values.title,
      body: values.body,
      channelId: values.channelId,
      user: values.userName
    }
    const users = values.to.split(',')

    db.ref('/tokens').once('value', (tokenSnapshot) => {
      let tokens = []
      tokenSnapshot.forEach((tokenSnapshotChild) => {
        let user = tokenSnapshotChild.key
        if (users.indexOf(user) >= 0) {
          tokens.push(tokenSnapshotChild.val())
        }
      })

      const message = {
        data: data,
        tokens: tokens
      }

      admin.messaging().sendMulticast(message)
        .then((response) => {
          res.json({"message": 'Successfully sent message:' + JSON.stringify(response)})
        })
        .catch((error) => {
          res.json({"message": 'Error sending message:' + JSON.stringify(error)})
        })
    })
  })
})

app.use('/push_message/topic', (req, res) => {
  db.ref('/fcm/topic').once('value', (snapshot) => {
    const values = snapshot.val()

    const data = {
      title: values.title,
      body: values.body,
      channelId: values.channelId
    }

    const message = {
      data: data,
      topic: 'server_message'
    }

    admin.messaging().send(message)
      .then((response) => {
        res.json({"message": 'Successfully sent topic message:' + JSON.stringify(response)})
      })
      .catch((error) => {
        res.json({"message": 'Error sending topic message:' + JSON.stringify(error)})
      })
  })
})

exports.app = functions.https.onRequest(app)
