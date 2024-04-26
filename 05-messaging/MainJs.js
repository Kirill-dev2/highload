const token = 'eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyZDczZDBkOS1mZjI4LTRiZWQtYmMyNC1jNGMwYTk2NWE1OWIiLCJzdWIiOiIwMDIzZjE2NC1hNDUwLTQ3MWYtODZiOS1mZjgxZTc4Njc4MmEiLCJpYXQiOjE3MTQzMjk3ODEsImV4cCI6MTcxNDMzMzM4MSwidXNlci1pZCI6IjAwMjNmMTY0LWE0NTAtNDcxZi04NmI5LWZmODFlNzg2NzgyYSIsInJlbW90ZS1hZGRyZXNzIjoiMDowOjA6MDowOjA6MDoxIn0.eVKFXo0k5BFeT2Q1x-z8DC16MxW-AFRRxoJZBJ-EIQ4';

class MainJs {

  constructor() {
    this._onConnected = this._onConnected.bind(this);
  }

  _onConnected(frame) {
    this.setConnected(true);
    console.log('Connected: ' + frame);
    let userId = frame.headers['user-name'];
    this.stompClient.subscribe('/queue/friends.posts.' + userId,
        this.showMessage);
  }

  setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('feed').style.visibility = connected ? 'visible'
        : 'hidden';
    document.getElementById('response').innerHTML = '';
  }

  connect() {
    var socket = new SockJS(
        'http://localhost:8080/post/feed/posted?Authorization=' + token);
    this.stompClient = Stomp.over(socket);
    this.stompClient.connect({headers: {Authorization: 'Bearer <TOKEN>'}},
        this._onConnected);
  }

  disconnect() {
    if (this.stompClient != null) {
      this.stompClient.disconnect();
    }
    this.setConnected(false);
    console.log("Disconnected");
  }

  showMessage(message) {
    var response = document.getElementById('response');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.appendChild(document.createTextNode(message.body));
    response.appendChild(p);
  }
}

var mainjs = new MainJs();
