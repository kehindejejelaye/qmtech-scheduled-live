// ======== DOM Elements ========
const statusEl = document.getElementById('status');
const connectBtn = document.getElementById('connectBtn');
const subscribeBtn = document.getElementById('subscribeBtn');
const createPollBtn = document.getElementById('createPollBtn');
const getPollBtn = document.getElementById('getPollBtn');
const voteBtn = document.getElementById('voteBtn');
const refreshPollsBtn = document.getElementById('refreshPollsBtn');
const pollsTableBody = document.querySelector('#pollsTable tbody');
const messagesEl = document.getElementById('messages');

const wsUrlEl = document.getElementById('wsUrl');
const pollIdEl = document.getElementById('pollId');
const pollQuestionEl = document.getElementById('pollQuestion');
const pollOptionsEl = document.getElementById('pollOptions');
const getPollIdEl = document.getElementById('getPollId');
const votePollIdEl = document.getElementById('votePollId');
const voteOptionEl = document.getElementById('voteOption');
const voteUsernameEl = document.getElementById('voteUsername'); // ✅ username input
const subscribedPollDetailsEl = document.getElementById('subscribedPollDetails'); // ✅ NEW container

let ws = null;

// ======== Utility ========
function addMessage(type, content, isJson = false) {
    const el = document.createElement('div');
    el.className = `message ${type}`;
    el.innerHTML = `<div class="message-type">${type}</div>`;
    const contentEl = document.createElement('div');
    contentEl.className = 'message-content';
    try {
        contentEl.textContent = isJson ? JSON.stringify(JSON.parse(content), null, 2) : content;
    } catch {
        contentEl.textContent = content;
    }
    el.appendChild(contentEl);
    messagesEl.appendChild(el);
    messagesEl.scrollTop = messagesEl.scrollHeight;
}

function updateStatus(status, text) {
    statusEl.className = `status ${status}`;
    statusEl.textContent = text;
}

// ======== UI Helpers ========
// ✅ Render subscribed poll details live
function renderSubscribedPollDetails(poll) {
    if (!subscribedPollDetailsEl) return;

    const optionsHtml = (poll.options || []).map(opt =>
        `<li>${opt.name} — <strong>${opt.votes}</strong> votes</li>`
    ).join('');

    subscribedPollDetailsEl.innerHTML = `
        <h3>${poll.question}</h3>
        <p><strong>Poll ID:</strong> ${poll.id}</p>
        <p><strong>Status:</strong> ${poll.status || 'N/A'}</p>
        <p><strong>Created:</strong> ${poll.createdAt ? new Date(poll.createdAt).toLocaleString() : 'N/A'}</p>
        ${poll.startTime ? `<p><strong>Start Time:</strong> ${new Date(poll.startTime).toLocaleString()}</p>` : ''}
        <h4>Options</h4>
        <ul>${optionsHtml}</ul>
    `;
}

// ======== WebSocket ========
// ======== WebSocket ========
function connectWebSocket() {
    const url = wsUrlEl.value.trim();
    if (!url) return addMessage('info','Please enter a WebSocket URL');
    updateStatus('connecting','🔄 Connecting...');
    connectBtn.disabled = true;

    ws = new WebSocket(url);
    ws.onopen = () => {
        updateStatus('connected','✅ Connected');
        connectBtn.textContent = 'Disconnect';
        connectBtn.disabled = false;
        subscribeBtn.disabled = false;
        addMessage('info',`Connected to ${url}`);
    };

    // ✅ Handle poll updates from server
    ws.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);

            // Your new "poll_update" event from backend
            if (data.type === 'poll_update') {
                const poll = {
                    id: data.id,
                    question: data.question,
                    options: data.options,
                    tallies: data.tallies,
                    scheduledStartTime: data.scheduledStartTime
                };

                // Show raw JSON in messages
                addMessage('received', JSON.stringify(poll), true);

                // Render in the subscribed poll details panel
                if (subscribedPollDetailsEl) {
                    subscribedPollDetailsEl.textContent =
                        JSON.stringify(poll, null, 2);
                }
            }
            // Fallback for other message types (keep old behaviour)
            else if (data.type === 'pollUpdate' && data.poll) {
                renderSubscribedPollDetails(data.poll);
            }
            else {
                addMessage('received', event.data, true);
            }
        } catch {
            addMessage('received', event.data);
        }
    };

    ws.onclose = e => {
        updateStatus('disconnected','❌ Disconnected');
        connectBtn.textContent = 'Connect to WebSocket';
        connectBtn.disabled = false;
        subscribeBtn.disabled = true;
        ws = null;
        addMessage('info',`Connection closed: ${e.code}`);
    };
    ws.onerror = err => addMessage('info','WebSocket error: ' + err);
}

function disconnectWebSocket() { if (ws) ws.close(); }

// ======== REST Actions ========
function createPoll() {
    const question = pollQuestionEl.value.trim();
    const options = pollOptionsEl.value
        .split(',')
        .map(o => o.trim())
        .filter(Boolean);

    const startTimeEl = document.getElementById('pollStartTime');
    const startTimeRaw = startTimeEl ? startTimeEl.value : '';

    if (!question || options.length < 2 || !startTimeRaw) {
        return addMessage('info', 'Provide question, at least 2 options, and a start time');
    }

    const scheduledStartTime = new Date(startTimeRaw).toISOString();

    fetch('/api/v1/polls', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question, options, scheduledStartTime })
    })
        .then(r => {
            if (!r.ok) throw new Error(`HTTP ${r.status}`);
            return r.json();
        })
        .then(data => {
            addMessage('info', 'Poll created!');
            pollIdEl.value = getPollIdEl.value = votePollIdEl.value = data.id;
            refreshPolls();
        })
        .catch(err => addMessage('info', 'Create failed: ' + err));
}

function getPoll(id) {
    const pollId = id || getPollIdEl.value.trim();
    if (!pollId) return addMessage('info','Enter poll ID');
    fetch(`/api/v1/polls/${pollId}`)
        .then(r=>r.json())
        .then(d=>{
            addMessage('received',JSON.stringify(d),true);
            // if this poll is the subscribed one, update details
            if (pollId === pollIdEl.value.trim()) renderSubscribedPollDetails(d);
        })
        .catch(err=>addMessage('info','Get failed: '+err));
}

function castVote() {
    const id = votePollIdEl.value.trim();
    const option = voteOptionEl.value.trim();
    const username = voteUsernameEl.value.trim();

    if (!id || !option || !username) {
        return addMessage('info','Provide Poll ID, Option, and Username');
    }

    fetch(`/api/v1/polls/${id}/vote`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ option, username })
    })
        .then(r => {
            if (r.status === 202) {
                addMessage('info', `Vote accepted. Thank you, ${username}!`);
                return;
            }
            return r.json();
        })
        .then(d => { if (d) addMessage('received', JSON.stringify(d), true); })
        .catch(err => addMessage('info', 'Vote failed: ' + err));
}

function refreshPolls() {
    fetch('/api/v1/polls')
        .then(r=>r.json())
        .then(data=>{
            pollsTableBody.innerHTML = '';
            data.forEach(p=>{
                const tr = document.createElement('tr');
                tr.innerHTML = `
                <td>${p.id}</td>
                <td>${p.question}</td>
                <td><button class="details-btn" data-id="${p.id}">Details</button></td>`;
                pollsTableBody.appendChild(tr);
            });
        })
        .catch(err=>addMessage('info','Failed to fetch polls: '+err));
}

// ======== Events ========
connectBtn.onclick = () => ws && ws.readyState===1 ? disconnectWebSocket() : connectWebSocket();
subscribeBtn.onclick = () => {
    if (ws && ws.readyState===1) {
        const pollId = pollIdEl.value.trim();
        if (!pollId) return addMessage('info','Enter poll ID to subscribe');
        ws.send(JSON.stringify({action:'subscribe', pollId}));
        addMessage('sent','Subscribed to '+pollId);
    }
};
createPollBtn.onclick = createPoll;
getPollBtn.onclick = ()=>getPoll();
voteBtn.onclick = castVote;
refreshPollsBtn.onclick = refreshPolls;

pollsTableBody.addEventListener('click', e=>{
    if (e.target.classList.contains('details-btn')) {
        const id = e.target.dataset.id;
        getPoll(id);
    }
});

refreshPolls();
addMessage('info','Client ready. Double-click a Poll ID field to generate new UUID.');
