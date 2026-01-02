// 配置
let API_ENDPOINT = localStorage.getItem('apiEndpoint') || 'http://localhost:8080/api/chat';
let chatHistory = [];

// DOM 元素
const messagesContainer = document.getElementById('messages');
const userInput = document.getElementById('userInput');
const sendButton = document.getElementById('sendButton');
const apiUrlDisplay = document.getElementById('apiUrl');
const settingsButton = document.getElementById('settingsButton');
const settingsPanel = document.getElementById('settingsPanel');
const apiEndpointInput = document.getElementById('apiEndpoint');
const saveSettingsButton = document.getElementById('saveSettings');

// 初始化
function init() {
    updateApiUrlDisplay();
    apiEndpointInput.value = API_ENDPOINT;

    // 事件監聽
    sendButton.addEventListener('click', sendMessage);
    userInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    settingsButton.addEventListener('click', () => {
        settingsPanel.classList.toggle('hidden');
    });

    saveSettingsButton.addEventListener('click', saveSettings);

    // 自動調整 textarea 高度
    userInput.addEventListener('input', () => {
        userInput.style.height = 'auto';
        userInput.style.height = userInput.scrollHeight + 'px';
    });
}

// 更新 API URL 顯示
function updateApiUrlDisplay() {
    apiUrlDisplay.textContent = `API: ${API_ENDPOINT}`;
}

// 保存設定
function saveSettings() {
    const newEndpoint = apiEndpointInput.value.trim();
    if (newEndpoint) {
        API_ENDPOINT = newEndpoint;
        localStorage.setItem('apiEndpoint', API_ENDPOINT);
        updateApiUrlDisplay();
        settingsPanel.classList.add('hidden');
        addMessage('bot', '✅ API 端點已更新！');
    }
}

// 發送訊息
async function sendMessage() {
    const message = userInput.value.trim();
    if (!message) return;

    // 顯示使用者訊息
    addMessage('user', message);
    userInput.value = '';
    userInput.style.height = 'auto';

    // 加入歷史記錄
    chatHistory.push({ role: 'user', content: message });

    // 顯示載入中
    const loadingId = addMessage('bot', '<span class="loading">思考中</span>');
    sendButton.disabled = true;

    try {
        // 呼叫 API
        const response = await fetch(API_ENDPOINT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                message: message,
                history: chatHistory.slice(0, -1) // 不包含當前訊息
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const data = await response.json();

        // 移除載入訊息
        removeMessage(loadingId);

        if (data.success) {
            // 顯示回應
            addMessage('bot', data.message);
            chatHistory.push({ role: 'assistant', content: data.message });
        } else {
            // 顯示錯誤
            addMessage('bot', `❌ 錯誤：${data.error || '未知錯誤'}\n\n${data.hint || ''}`);
        }

    } catch (error) {
        // 移除載入訊息
        removeMessage(loadingId);

        // 顯示網路錯誤
        let errorMsg = '❌ 連線失敗！\n\n';
        if (error.message.includes('Failed to fetch')) {
            errorMsg += '無法連接到 API 端點。請確認：\n';
            errorMsg += '1. 後端伺服器是否已啟動\n';
            errorMsg += '2. API 端點設定是否正確\n';
            errorMsg += `3. 當前端點：${API_ENDPOINT}`;
        } else {
            errorMsg += `錯誤訊息：${error.message}`;
        }
        addMessage('bot', errorMsg);
    } finally {
        sendButton.disabled = false;
        userInput.focus();
    }
}

// 新增訊息到聊天視窗
function addMessage(role, content) {
    const messageId = `msg-${Date.now()}-${Math.random()}`;
    const messageDiv = document.createElement('div');
    messageDiv.id = messageId;
    messageDiv.className = `message ${role}-message`;

    const messageContent = document.createElement('div');
    messageContent.className = 'message-content';

    const roleName = role === 'user' ? '你' : 'AI 助手';
    messageContent.innerHTML = `
        <strong>${roleName}</strong>
        ${formatMessage(content)}
    `;

    messageDiv.appendChild(messageContent);
    messagesContainer.appendChild(messageDiv);

    // 滾動到底部
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    return messageId;
}

// 移除訊息
function removeMessage(messageId) {
    const message = document.getElementById(messageId);
    if (message) {
        message.remove();
    }
}

// 格式化訊息（支援簡單的 Markdown）
function formatMessage(text) {
    // 轉換換行
    text = text.replace(/\n/g, '<br>');

    // 轉換粗體
    text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

    // 轉換列表
    text = text.replace(/^- (.+)$/gm, '<li>$1</li>');
    text = text.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');

    return `<p>${text}</p>`;
}

// 啟動應用
init();
