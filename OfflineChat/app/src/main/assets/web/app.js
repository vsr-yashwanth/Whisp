document.addEventListener('DOMContentLoaded', () => {
    const refreshBtn = document.getElementById('refresh-btn');
    const refreshDbBtn = document.getElementById('refresh-db-btn');
    const clearDbBtn = document.getElementById('clear-db-btn');
    const sendBtn = document.getElementById('send-btn');
    const targetConvInput = document.getElementById('target-conv');
    const msgInput = document.getElementById('msg-input');
    const sendStatus = document.getElementById('send-status');

    const devicesContainer = document.getElementById('devices-container');
    const dbContainer = document.getElementById('db-container');
    const msgCount = document.getElementById('msg-count');
    const connCount = document.getElementById('conn-count');
    const gatewayStatus = document.getElementById('gateway-status');
    const gatewayBadge = document.getElementById('gateway-badge');

    let currentDevices = [];

    // Radar Canvas Setup
    const canvas = document.getElementById('mesh-radar-canvas');
    const ctx = canvas.getContext('2d');
    let sweepAngle = 0;
    let pulseRadius = 0;

    function drawRadar() {
        const width = canvas.width;
        const height = canvas.height;
        const centerX = width / 2;
        const centerY = height / 2;
        const maxRadius = Math.min(width, height) / 2 - 10;

        ctx.clearRect(0, 0, width, height);

        // Minimalist Grid Lines
        ctx.strokeStyle = '#12141A';
        ctx.lineWidth = 1;
        for (let x = 0; x < width; x += 40) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, height);
            ctx.stroke();
        }
        for (let y = 0; y < height; y += 40) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(width, y);
            ctx.stroke();
        }

        // Concentric Range Rings
        const rings = [0.25, 0.5, 0.75, 1.0];
        ctx.strokeStyle = '#1E2128';
        ctx.lineWidth = 1;
        rings.forEach(r => {
            ctx.beginPath();
            ctx.arc(centerX, centerY, maxRadius * r, 0, Math.PI * 2);
            ctx.stroke();
        });

        // Crosshairs
        ctx.strokeStyle = '#1E2128';
        ctx.beginPath();
        ctx.moveTo(centerX - maxRadius, centerY);
        ctx.lineTo(centerX + maxRadius, centerY);
        ctx.moveTo(centerX, centerY - maxRadius);
        ctx.lineTo(centerX, centerY + maxRadius);
        ctx.stroke();

        // Expanding Pulse
        pulseRadius = (pulseRadius + 0.006) % 1;
        ctx.strokeStyle = `rgba(255, 255, 255, ${(1 - pulseRadius) * 0.15})`;
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.arc(centerX, centerY, maxRadius * pulseRadius, 0, Math.PI * 2);
        ctx.stroke();

        // Sweeping Radar Beam
        sweepAngle = (sweepAngle + 0.03) % (Math.PI * 2);
        const beamX = centerX + Math.cos(sweepAngle) * maxRadius;
        const beamY = centerY + Math.sin(sweepAngle) * maxRadius;

        const gradient = ctx.createLinearGradient(centerX, centerY, beamX, beamY);
        gradient.addColorStop(0, 'rgba(255, 255, 255, 0.35)');
        gradient.addColorStop(1, 'transparent');

        ctx.strokeStyle = gradient;
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.lineTo(beamX, beamY);
        ctx.stroke();

        // Center Origin Node (White Dot)
        ctx.fillStyle = '#FFFFFF';
        ctx.beginPath();
        ctx.arc(centerX, centerY, 4, 0, Math.PI * 2);
        ctx.fill();

        // Render Mesh Nodes
        currentDevices.forEach((dev, idx) => {
            const angle = (idx * (Math.PI * 2 / Math.max(currentDevices.length, 1))) + 0.8;
            const dist = maxRadius * (0.45 + (idx % 3) * 0.2);
            const nx = centerX + Math.cos(angle) * dist;
            const ny = centerY + Math.sin(angle) * dist;

            const isGlobal = dev.id.startsWith('Global');
            const blipColor = isGlobal ? '#10B981' : '#FFFFFF';

            // Link Line
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.1)';
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.moveTo(centerX, centerY);
            ctx.lineTo(nx, ny);
            ctx.stroke();

            // Halo
            ctx.fillStyle = isGlobal ? 'rgba(16, 185, 129, 0.15)' : 'rgba(255, 255, 255, 0.12)';
            ctx.beginPath();
            ctx.arc(nx, ny, 8, 0, Math.PI * 2);
            ctx.fill();

            // Blip Dot
            ctx.fillStyle = blipColor;
            ctx.beginPath();
            ctx.arc(nx, ny, 3.5, 0, Math.PI * 2);
            ctx.fill();

            // Label
            ctx.fillStyle = '#8B92A0';
            ctx.font = '10px -apple-system, sans-serif';
            ctx.fillText(dev.name, nx + 10, ny + 3);
        });

        requestAnimationFrame(drawRadar);
    }

    requestAnimationFrame(drawRadar);

    // Fetch Stats
    const fetchStats = async () => {
        try {
            const res = await fetch('/api/stats');
            const data = await res.json();
            msgCount.textContent = data.messageCount;
            connCount.textContent = data.activePeers + 1;

            if (data.isGlobalGatewayActive) {
                gatewayStatus.textContent = 'ONLINE';
                gatewayBadge.textContent = 'Decentralized Gateway Active';
            } else {
                gatewayStatus.textContent = 'LOCAL';
                gatewayBadge.textContent = 'P2P Mesh Only';
            }
        } catch (e) {
            console.error('Failed to fetch stats', e);
        }
    };

    // Fetch Mesh Devices
    const fetchDevices = async () => {
        try {
            const res = await fetch('/api/devices');
            const devices = await res.json();
            currentDevices = devices;
            
            if (devices.length === 0) {
                devicesContainer.innerHTML = '<div class="loading">Scanning for peers...</div>';
            } else {
                devicesContainer.innerHTML = devices.map(device => {
                    const isGlobal = device.id.startsWith('Global');
                    const badge = isGlobal ? 'GLOBAL' : (device.name.includes('Bridge') ? 'BRIDGE' : 'BLE');

                    return `
                        <div class="device-item">
                            <div class="device-info">
                                <h4>${escapeHtml(device.name)}</h4>
                                <p>ID: ${escapeHtml(device.id)}</p>
                            </div>
                            <div style="display:flex; align-items:center; gap:8px;">
                                <span class="tag">${badge}</span>
                                <div class="status-indicator">
                                    <div class="status-dot"></div>
                                    ${escapeHtml(device.status)}
                                </div>
                            </div>
                        </div>
                    `;
                }).join('');
            }
        } catch (e) {
            console.error('Failed to fetch devices', e);
            devicesContainer.innerHTML = '<div class="loading">Failed to connect to Local Node API</div>';
        }
    };

    // Fetch MongoDB-formatted message documents + Hop Trace
    const fetchDbMessages = async () => {
        try {
            const res = await fetch('/api/db/messages');
            const docs = await res.json();
            
            if (docs.length === 0) {
                dbContainer.innerHTML = '<div class="loading">No records in SQLite database collection.</div>';
            } else {
                dbContainer.innerHTML = docs.map(doc => {
                    const dateStr = new Date(doc.timestamp).toLocaleTimeString();
                    let hopItemsHtml = '';

                    try {
                        const hops = JSON.parse(doc.hopTrace || '[]');
                        if (hops.length > 0) {
                            hopItemsHtml = `
                                <div class="hop-trail">
                                    <span style="font-weight:600; color:#8B92A0;">Route:</span>
                                    ${hops.map((h, i) => `<span class="hop-item">${i + 1}. ${escapeHtml(h.nodeName || h.nodeId)} (${escapeHtml(h.transport || 'P2P')})</span>`).join(' ➔ ')}
                                </div>
                            `;
                        }
                    } catch (e) {}

                    return `
                        <div class="doc-card">
                            <div class="doc-field"><span class="key">_id:</span> <span class="val-id">"${escapeHtml(doc._id)}"</span></div>
                            <div class="doc-field"><span class="key">conversation:</span> <span class="val-str">"${escapeHtml(doc.conversationId)}"</span></div>
                            <div class="doc-field"><span class="key">senderId:</span> <span class="val-str">"${escapeHtml(doc.senderId)}"</span></div>
                            <div class="doc-field"><span class="key">decryptedText:</span> <span class="val-str" style="font-weight:600; color:#FFFFFF;">"${escapeHtml(doc.decryptedText)}"</span></div>
                            <div class="doc-field"><span class="key">encryptedPayload:</span> <span class="val-str" style="opacity:0.4;">"${escapeHtml(doc.encryptedPayload.slice(0, 35))}..."</span></div>
                            <div class="doc-field"><span class="key">timestamp:</span> <span class="val-num">${doc.timestamp}</span> <span style="color:#5A6070;">// ${dateStr}</span></div>
                            <div class="doc-field"><span class="key">status:</span> <span class="val-str">"${escapeHtml(doc.status)}"</span></div>
                            ${hopItemsHtml}
                        </div>
                    `;
                }).join('');
            }
        } catch (e) {
            console.error('Failed to query DB messages', e);
            dbContainer.innerHTML = '<div class="loading">Error reading database collection.</div>';
        }
    };

    // Send Message from Web
    const sendMessage = async () => {
        const text = msgInput.value.trim();
        const conv = targetConvInput.value.trim() || 'General Chat';
        if (!text) return;

        sendBtn.disabled = true;
        sendStatus.textContent = 'Encrypting & broadcasting...';
        sendStatus.style.color = '#8B92A0';

        try {
            const res = await fetch('/api/db/messages/send', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ conversationId: conv, text: text })
            });
            const data = await res.json();
            if (data.success) {
                sendStatus.textContent = '✓ Message delivered to mesh';
                sendStatus.style.color = '#10B981';
                msgInput.value = '';
                fetchStats();
                fetchDbMessages();
            } else {
                sendStatus.textContent = 'Failed: ' + (data.message || 'Unknown error');
                sendStatus.style.color = '#F87171';
            }
        } catch (e) {
            sendStatus.textContent = 'Connection Error';
            sendStatus.style.color = '#F87171';
        }

        sendBtn.disabled = false;
        setTimeout(() => { sendStatus.textContent = ''; }, 3000);
    };

    // Clear DB
    const clearDb = async () => {
        if (!confirm('Clear all encrypted messages?')) return;
        try {
            await fetch('/api/db/clear', { method: 'POST' });
            fetchStats();
            fetchDbMessages();
        } catch (e) {
            console.error('Failed to clear database', e);
        }
    };

    function escapeHtml(str) {
        if (!str) return '';
        return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    sendBtn.addEventListener('click', sendMessage);
    msgInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    refreshBtn.addEventListener('click', () => {
        fetchStats();
        fetchDevices();
    });

    refreshDbBtn.addEventListener('click', () => {
        fetchStats();
        fetchDbMessages();
    });

    clearDbBtn.addEventListener('click', clearDb);

    // Initial load
    fetchStats();
    fetchDevices();
    fetchDbMessages();

    // Auto-refresh every 3.5 seconds
    setInterval(() => {
        fetchStats();
        fetchDevices();
        fetchDbMessages();
    }, 3500);
});
