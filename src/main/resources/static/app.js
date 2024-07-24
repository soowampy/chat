let stompClient;
let currentRoomId;
let userId;
let userNickname;
let subscription;

document.addEventListener("DOMContentLoaded", function() {
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function(frame) {
    // 채팅방 목록 구독, 새로 생성된 방을 추가
    stompClient.subscribe('/topic/chatrooms', function(messageOutput) {
      const room = JSON.parse(messageOutput.body);
      addRoomToList(room);
    });
  });

  // 퇴장 - 채팅방 떠나는 메세지
  window.addEventListener("beforeunload", function () {
    if (currentRoomId) {
      leaveRoom(true);
    }
  });
});

// 닉네임 설정, 화면 전환
function setNickname() {
  userNickname = document.getElementById('nickname').value;
  if (userNickname) {
    document.getElementById('nickname-input').style.display = 'none';
    document.getElementById('app').style.display = 'block';
    fetchRooms();
  } else {
    alert('닉네임을 입력해주세요');
  }
}

// 서버에서 채팅방 목록을 가져와서 출력
function fetchRooms() {
  fetch('/api/chatrooms')
  .then(response => response.json())
  .then(rooms => {
    const roomList = document.getElementById('rooms');
    roomList.innerHTML = '';
    rooms.forEach(room => {
      addRoomToList(room);
    });
  });
}

// 채팅방 목록에 방을 추가
function addRoomToList(room) {
  const roomList = document.getElementById('rooms');
  const li = document.createElement('li');
  li.textContent = `${room.title} (${room.activeUsers} 명)`;
  li.onclick = () => joinRoom(room.id, room.title, room.activeUsers); // 클릭 이벤트 -> 채팅방 입장
  li.setAttribute('data-room-id', room.id); // 방 ID를 데이터 속성으로 저장
  roomList.appendChild(li);

  // 접속자 수 업데이트 구독
  stompClient.subscribe(`/topic/activeUsers/${room.id}`, function(activeUsersMessage) {
    updateActiveUsersCount(room.id, JSON.parse(activeUsersMessage.body));
  });
}

// 채팅방 생성하고 입장
function createRoom() {
  const title = document.getElementById('room-title').value;
  fetch('/api/chatrooms', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(title)
  }).then(response => response.json())
  .then(room => {
    joinRoom(room.id, room.title, room.activeUsers);
  });
}

// 입장
function joinRoom(roomId, roomTitle, activeUsers) {
  if (subscription) {
    subscription.unsubscribe();
  }

  currentRoomId = roomId;
  userId = generateUUID();
  document.getElementById('app').style.display = 'none';
  document.getElementById('chat').style.display = 'block';
  document.getElementById('room-name').textContent = `${roomTitle} (${activeUsers} 명)`;

  // 이전 메시지 표시
  fetch(`/api/messages/chatroom/${roomId}`)
  .then(response => response.json())
  .then(messages => {
    const messagesList = document.getElementById('messages');
    messagesList.innerHTML = '';
    messages.forEach(message => {
      showMessage(message);
    });
  });

  // 입장 메세지 전송
  stompClient.send(`/app/chat.joinRoom/${roomId}`, {}, JSON.stringify({ userId: userId, nickname: userNickname }));

  // 메세지 구독
  subscription = stompClient.subscribe(`/topic/${roomId}`, function(messageOutput) {
    if (JSON.parse(messageOutput.body) != null && JSON.parse(messageOutput.body).body != null) {
      showMessage(JSON.parse(messageOutput.body).body);
    } else if(JSON.parse(messageOutput.body) != null) {
      showMessage(JSON.parse(messageOutput.body));
    } else {
      alert("채팅 서버 오류입니다.");
    }
  });

  // 접속자 수 업데이트 구독
  stompClient.subscribe(`/topic/activeUsers/${roomId}`, function(activeUsersMessage) {
    updateActiveUsersCount(roomId, JSON.parse(activeUsersMessage.body));
  });
}

// 접속자 수 업데이트
function updateActiveUsersCount(roomId, activeUsersCount) {
  const roomListItems = document.querySelectorAll('#rooms li');
  roomListItems.forEach(item => {
    if (item.getAttribute('data-room-id') === roomId) {
      const roomTitle = item.textContent.split(' (')[0];
      item.textContent = `${roomTitle} (${activeUsersCount} 명)`;
    }
  });

  if (currentRoomId === roomId) {
    const roomNameElement = document.getElementById('room-name');
    const currentRoomTitle = roomNameElement.textContent.split(' (')[0];
    roomNameElement.textContent = `${currentRoomTitle} (${activeUsersCount} 명)`;
  }
}

// 입력한 메세지 서버로 전송
function sendMessage() {
  const input = document.getElementById('message-input');
  const message = {
    chatRoomId: currentRoomId,
    userId: userId,
    nickname: userNickname,
    content: input.value,
    createdAt: new Date().toISOString(),
    isSystemMessage: false
  };
  console.log('Send message : ', message);
  stompClient.send(`/app/chat.sendMessage/${currentRoomId}`, {}, JSON.stringify(message));
  input.value = '';
}

// 서버에서 받은 메세지 화면에 표시
function showMessage(message) {
  console.log('Received message:', message);
  const messages = document.getElementById('messages');
  const li = document.createElement('li');
  const timestamp = new Date(message.createdAt).toLocaleTimeString();
  if (message.isSystemMessage) {
    li.textContent = `${message.content} (${timestamp})`;
    li.style.fontStyle = "italic";
    li.style.color = "gray";
  } else {
    li.textContent = `${message.nickname || message.userId}: ${message.content} (${timestamp})`;
  }
  messages.appendChild(li);
}

// 채팅방 퇴장
function leaveRoom(isUnload = false) {
  const leaveMessage = {
    chatRoomId: currentRoomId,
    userId: userId,
    nickname: userNickname,
    content: userNickname + "님이 채팅방을 나갔습니다 ---",
    createdAt: new Date().toISOString(),
    isSystemMessage: true
  };
  stompClient.send(`/app/chat.leaveRoom/${currentRoomId}`, {}, JSON.stringify(leaveMessage));

  if (subscription) {
    subscription.unsubscribe();
    subscription = null;
  }

  if (!isUnload) {
    document.getElementById('app').style.display = 'block';
    document.getElementById('chat').style.display = 'none';
    currentRoomId = null;
    const messagesList = document.getElementById('messages');
    messagesList.innerHTML = '';
  }
}

// uuid 생성
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}
