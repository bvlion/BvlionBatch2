const functions = require('firebase-functions')
const express = require('express')
const basicAuth = require('basic-auth-connect')
const admin = require('firebase-admin')
admin.initializeApp(functions.config().firebase)

const app = express()
app.all('/*', basicAuth((user, password) => user === "username_replace" && password === "password_replace"))
app.use(express.static(__dirname + '/static/'))
exports.app = functions.https.onRequest(app)

const db = admin.database()

let tokenEnable = false
let topicEnable = false

db.ref('/fcm/token/date').on('value', (changedSnapshot) => {
  if (!tokenEnable) {
    tokenEnable = true
    return
  }
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
          console.log('Successfully sent message:', response)
        })
        .catch((error) => {
          console.log('Error sending message:', error)
        })
    })
  })
})

db.ref('/fcm/topic/date').on('value', (changedSnapshot) => {
  if (!topicEnable) {
    topicEnable = true
    return
  }
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
        console.log('Successfully sent topic message:', response)
      })
      .catch((error) => {
        console.log('Error sending topic message:', error)
      })
  })
})