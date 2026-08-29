// Whisp Admin Control Plane v4 - JavaScript Engine

document.addEventListener('DOMContentLoaded', () => {
    initAuth();
    initNavigation();
    initRadarCanvas();
    initTopologyCanvas();
    initSimulationEngine();
    initEmergencyControls();
    initSearchFilters();

    // Check existing auth session
    if (sessionStorage.getItem('whisp_admin_token')) {
        showDashboard();
    } else {
        showLogin();
    }

    // Live refresh loop every 3 seconds
    setInterval(() => {
        if (sessionStorage.getItem('whisp_admin_token')) {
            fetchAllTelemetry();
        }
    }, 3000);
});

// State
let globalNodes = [];
let globalRoutes = [];
let pendingActionNodeId = null;

// ==========================================
// 0. AUTHENTICATION & LOGIN
// ==========================================
function initAuth() {
    const form = document.getElementById('admin-login-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('admin-user-input').value.trim();
        const password = document.getElementById('admin-pass-input').value.trim();
        const errorMsg = document.getElementById('login-error-msg');
        const submitBtn = document.getElementById('login-submit-btn');

        errorMsg.classList.add('hidden');
        submitBtn.disabled = true;
        submitBtn.textContent = 'AUTHENTICATING...';

        try {
            // Try Ktor server auth first, fallback to relay auth port 8088
            let res = await fetch('/api/v1/auth/admin-login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!res.ok) {
                try {
                    res = await fetch('http://127.0.0.1:8088/api/auth/admin-login', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ username, password })
                    });
                } catch (err) {}
            }

            if (res.ok) {
                const data = await res.json();
                sessionStorage.setItem('whisp_admin_token', data.token || 'whisp_auth_ok');
                sessionStorage.setItem('whisp_admin_user', data.username || username);
                sessionStorage.setItem('whisp_admin_role', data.role || 'SUPER_ADMIN');
                showDashboard();
            } else {
                errorMsg.textContent = 'Invalid admin credentials or unauthorized account.';
                errorMsg.classList.remove('hidden');
            }
        } catch (err) {
            errorMsg.textContent = 'Error connecting to authentication service: ' + err.message;
            errorMsg.classList.remove('hidden');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'AUTHENTICATE & ENTER';
        }
    });

    document.getElementById('admin-logout-btn')?.addEventListener('click', () => {
        sessionStorage.clear();
        showLogin();
    });
}

function showDashboard() {
    document.getElementById('admin-login-view')?.classList.add('hidden');
    document.getElementById('admin-dashboard-view')?.classList.remove('hidden');
    
    const user = sessionStorage.getItem('whisp_admin_user') || 'admin';
    const role = sessionStorage.getItem('whisp_admin_role') || 'SUPER_ADMIN';
    const usernameElem = document.getElementById('current-username');
    if (usernameElem) usernameElem.textContent = `${user} (${role})`;
    
    fetchAllTelemetry();
}

function showLogin() {
    document.getElementById('admin-dashboard-view')?.classList.add('hidden');
    document.getElementById('admin-login-view')?.classList.remove('hidden');
}

function fillAdminCreds(u, p) {
    document.getElementById('admin-user-input').value = u;
    document.getElementById('admin-pass-input').value = p;
}

// ==========================================
// 1. TAB NAVIGATION
// ==========================================
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    const tabPanes = document.querySelectorAll('.tab-pane');
    const pageTitle = document.getElementById('page-title');

    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const targetTab = item.getAttribute('data-tab');

            navItems.forEach(n => n.classList.remove('active'));
            tabPanes.forEach(p => p.classList.remove('active'));

            item.classList.add('active');
            const activePane = document.getElementById(`tab-${targetTab}`);
            if (activePane) activePane.classList.add('active');

            if (pageTitle) {
                pageTitle.textContent = item.textContent.trim();
            }

            // Trigger canvas redraws when switching tabs
            if (targetTab === 'topology') {
                drawInteractiveTopology();
            }
        });
    });
}

// ==========================================
// 2. FETCH ALL TELEMETRY
// ==========================================
async function fetchAllTelemetry() {
    fetchDashboard();
    fetchNodes();
    fetchRoutes();
    fetchDtn();
    fetchPartitions();
    fetchCrdt();
    fetchSecurity();
    fetchAudit();
}

async function fetchDashboard() {
    try {
        const res = await fetch('/api/v1/admin/dashboard');
        if (!res.ok) return;
        const data = await res.json();

        document.getElementById('health-score').textContent = data.healthScore;
        document.getElementById('active-nodes').textContent = data.activeNodesCount;
        document.getElementById('active-peers-sub').textContent = `${data.connectedPeersCount} Connected Peers`;
        document.getElementById('dtn-count').textContent = data.dtnStoredBundles;
        
        const kbUsed = Math.round(data.dtnStorageBytesUsed / 1024);
        document.getElementById('dtn-storage-sub').textContent = `${kbUsed} KB / 500 MB`;
        document.getElementById('delivery-rate').textContent = `${data.deliveryRatePercent.toFixed(1)}%`;
        document.getElementById('avg-latency').textContent = `${Math.round(data.averageLatencyMs)}ms`;
        document.getElementById('top-epoch').textContent = data.currentNetworkEpoch;
        document.getElementById('global-status-text').textContent = data.status;

        const threatPill = document.getElementById('threat-level');
        if (threatPill) threatPill.textContent = data.securityThreatLevel;
    } catch (e) {
        console.warn('Dashboard fetch error', e);
    }
}

async function fetchNodes() {
    try {
        const res = await fetch('/api/v1/admin/nodes');
        if (!res.ok) return;
        globalNodes = await res.json();

        const tbody = document.getElementById('nodes-table-body');
        if (!tbody) return;

        if (globalNodes.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="loading-td">No external mesh nodes discovered yet. Running standalone node.</td></tr>`;
            return;
        }

        tbody.innerHTML = globalNodes.map(node => `
            <tr>
                <td><code>${node.id.substring(0, 14)}...</code></td>
                <td><strong>${escapeHtml(node.name)}</strong></td>
                <td><span class="tag">${node.transport}</span></td>
                <td>
                    <span class="${node.predictedStabilityPct >= 80 ? 'text-emerald' : 'text-amber'}">
                        ${node.predictedStabilityPct}%
                    </span>
                </td>
                <td>
                    <span class="${node.isIsolated ? 'badge-danger' : 'badge-emerald'}">
                        ${node.status}
                    </span>
                </td>
                <td>
                    ${node.isIsolated 
                        ? `<button class="btn secondary-btn" onclick="restoreNode('${node.id}')">Restore</button>`
                        : `<button class="btn danger-btn" onclick="openIsolateModal('${node.id}', '${escapeHtml(node.name)}')">Isolate</button>`
                    }
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.warn('Nodes fetch error', e);
    }
}

async function fetchRoutes() {
    try {
        const res = await fetch('/api/v1/admin/routes');
        if (!res.ok) return;
        globalRoutes = await res.json();

        const tbody = document.getElementById('routes-table-body');
        if (!tbody) return;

        if (globalRoutes.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="loading-td">No active multi-hop routing paths.</td></tr>`;
            return;
        }

        tbody.innerHTML = globalRoutes.map(r => `
            <tr>
                <td><code>${r.destination.substring(0, 12)}...</code></td>
                <td><strong>${escapeHtml(r.nextHopName)}</strong> (<code>${r.nextHop.substring(0, 8)}</code>)</td>
                <td><span class="tag">${r.transport}</span></td>
                <td>${r.latencyMs}ms</td>
                <td><strong class="text-emerald">${r.stabilityPct}%</strong></td>
                <td><small style="color: var(--text-secondary);">${escapeHtml(r.explanation)}</small></td>
            </tr>
        `).join('');
    } catch (e) {
        console.warn('Routes fetch error', e);
    }
}

async function fetchDtn() {
    try {
        const res = await fetch('/api/v1/admin/dtn');
        if (!res.ok) return;
        const data = await res.json();

        const kbUsed = Math.round(data.storageBytesUsed / 1024);
        const percent = Math.min(100, Math.max(1, (data.storageBytesUsed / data.storageLimitBytes) * 100));
        
        document.getElementById('dtn-progress-fill').style.width = `${percent}%`;
        document.getElementById('dtn-quota-text').textContent = `${kbUsed} KB / 500 MB used (${data.totalBundles} bundles)`;

        const tbody = document.getElementById('dtn-table-body');
        if (!tbody) return;

        if (data.bundles.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="loading-td">No bundles in DTN custody storage.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.bundles.map(b => `
            <tr>
                <td><code>${b.bundleId.substring(0, 10)}...</code></td>
                <td>${escapeHtml(b.source.substring(0, 12))}</td>
                <td>${escapeHtml(b.destination.substring(0, 12))}</td>
                <td><span class="badge-emerald">${b.custodyState}</span></td>
                <td>${b.ttl}s</td>
                <td>${b.replicationCount}</td>
                <td>${(b.deliveryProbability * 100).toFixed(0)}%</td>
            </tr>
        `).join('');
    } catch (e) {
        console.warn('DTN fetch error', e);
    }
}

async function fetchPartitions() {
    try {
        const res = await fetch('/api/v1/admin/partitions');
        if (!res.ok) return;
        const data = await res.json();

        document.getElementById('partition-epoch-val').textContent = data.currentEpoch;
        const pill = document.getElementById('partition-state-pill');
        if (pill) {
            pill.textContent = data.isPartitioned ? 'PARTITION SPLIT DETECTED' : 'SINGLE UNIFIED MESH';
            pill.className = data.isPartitioned ? 'epoch-state-pill badge-danger' : 'epoch-state-pill';
        }
        document.getElementById('reconcile-status-text').textContent = data.reconciliationStatus;
    } catch (e) {
        console.warn('Partitions fetch error', e);
    }
}

async function fetchCrdt() {
    try {
        const res = await fetch('/api/v1/admin/crdt');
        if (!res.ok) return;
        const data = await res.json();

        const tbody = document.getElementById('crdt-table-body');
        if (!tbody) return;

        if (data.documents.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="loading-td">No active CRDT documents registered.</td></tr>`;
            return;
        }

        tbody.innerHTML = data.documents.map(d => `
            <tr>
                <td><strong>${escapeHtml(d.documentId)}</strong></td>
                <td>${d.keysCount}</td>
                <td><code>${d.keys.join(', ')}</code></td>
                <td><span class="badge-emerald">LWW-MAP ACTIVE</span></td>
            </tr>
        `).join('');
    } catch (e) {
        console.warn('CRDT fetch error', e);
    }
}

async function fetchSecurity() {
    try {
        const res = await fetch('/api/v1/admin/security/events');
        if (!res.ok) return;
        const events = await res.json();

        const tbody = document.getElementById('security-table-body');
        if (!tbody) return;

        tbody.innerHTML = events.map(e => {
            const time = new Date(e.timestamp).toLocaleTimeString();
            return `
                <tr>
                    <td><code>${e.id}</code></td>
                    <td>${time}</td>
                    <td><span class="${e.severity === 'CRITICAL' ? 'badge-danger' : 'badge-emerald'}">${e.severity}</span></td>
                    <td><span class="tag">${e.category}</span></td>
                    <td>${escapeHtml(e.description)}</td>
                </tr>
            `;
        }).join('');
    } catch (e) {
        console.warn('Security fetch error', e);
    }
}

async function fetchAudit() {
    try {
        const res = await fetch('/api/v1/admin/audit');
        if (!res.ok) return;
        const logs = await res.json();

        const tbody = document.getElementById('audit-table-body');
        if (!tbody) return;

        tbody.innerHTML = logs.map(l => {
            const time = new Date(l.timestamp).toLocaleTimeString();
            return `
                <tr>
                    <td>${time}</td>
                    <td><strong>${escapeHtml(l.adminIdentity)}</strong></td>
                    <td><span class="tag">${l.action}</span></td>
                    <td><code>${escapeHtml(l.resource)}</code></td>
                    <td><span class="badge-emerald">${l.result}</span></td>
                    <td><small style="color: var(--text-secondary);">${escapeHtml(l.reason)}</small></td>
                </tr>
            `;
        }).join('');
    } catch (e) {
        console.warn('Audit fetch error', e);
    }
}

// ==========================================
// 3. OVERVIEW RADAR CANVAS
// ==========================================
let radarAngle = 0;
function initRadarCanvas() {
    const canvas = document.getElementById('overview-radar-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        const cx = canvas.width / 2;
        const cy = canvas.height / 2;
        const maxR = Math.min(cx, cy) - 20;

        // Draw Rings
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.08)';
        ctx.lineWidth = 1;
        for (let i = 1; i <= 3; i++) {
            ctx.beginPath();
            ctx.arc(cx, cy, (maxR / 3) * i, 0, Math.PI * 2);
            ctx.stroke();
        }

        // Draw Crosshairs
        ctx.beginPath();
        ctx.moveTo(cx - maxR, cy);
        ctx.lineTo(cx + maxR, cy);
        ctx.moveTo(cx, cy - maxR);
        ctx.lineTo(cx, cy + maxR);
        ctx.stroke();

        // Draw Sweeping Line
        radarAngle += 0.03;
        const endX = cx + maxR * Math.cos(radarAngle);
        const endY = cy + maxR * Math.sin(radarAngle);

        ctx.strokeStyle = 'rgba(16, 185, 129, 0.4)';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.lineTo(endX, endY);
        ctx.stroke();

        // Center Origin Node
        ctx.fillStyle = '#ffffff';
        ctx.beginPath();
        ctx.arc(cx, cy, 4, 0, Math.PI * 2);
        ctx.fill();

        // Draw Discovered Nodes
        globalNodes.forEach((node, idx) => {
            const angle = (idx * (360 / Math.max(1, globalNodes.length))) * (Math.PI / 180);
            const dist = maxR * (0.5 + (idx % 2) * 0.3);
            const px = cx + dist * Math.cos(angle);
            const py = cy + dist * Math.sin(angle);

            ctx.fillStyle = node.isIsolated ? '#ef4444' : '#10b981';
            ctx.beginPath();
            ctx.arc(px, py, 5, 0, Math.PI * 2);
            ctx.fill();
        });

        requestAnimationFrame(draw);
    }
    requestAnimationFrame(draw);
}

// ==========================================
// 4. INTERACTIVE 2D TOPOLOGY CANVAS
// ==========================================
function initTopologyCanvas() {
    const canvas = document.getElementById('interactive-topo-canvas');
    if (!canvas) return;

    document.getElementById('topo-reset-btn')?.addEventListener('click', () => {
        drawInteractiveTopology();
    });
}

function drawInteractiveTopology() {
    const canvas = document.getElementById('interactive-topo-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const cx = canvas.width / 2;
    const cy = canvas.height / 2;

    // Draw Central Local Node
    ctx.fillStyle = '#ffffff';
    ctx.beginPath();
    ctx.arc(cx, cy, 14, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = '#ffffff';
    ctx.font = '11px JetBrains Mono';
    ctx.fillText('Local Node (Hub)', cx - 45, cy + 28);

    // Draw Peer Nodes around hub
    globalNodes.forEach((node, i) => {
        const angle = (i * (360 / Math.max(1, globalNodes.length))) * (Math.PI / 180);
        const dist = 160 + (i % 2) * 60;
        const nx = cx + dist * Math.cos(angle);
        const ny = cy + dist * Math.sin(angle);

        // Link line
        ctx.strokeStyle = node.isIsolated ? 'rgba(239, 68, 68, 0.4)' : 'rgba(16, 185, 129, 0.4)';
        ctx.lineWidth = 2;
        ctx.setLineDash([4, 4]);
        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.lineTo(nx, ny);
        ctx.stroke();
        ctx.setLineDash([]);

        // Node Circle
        ctx.fillStyle = node.isIsolated ? '#ef4444' : '#10b981';
        ctx.beginPath();
        ctx.arc(nx, ny, 10, 0, Math.PI * 2);
        ctx.fill();

        // Node Label
        ctx.fillStyle = '#94a3b8';
        ctx.font = '10px JetBrains Mono';
        ctx.fillText(node.name, nx - 30, ny + 20);
    });
}

// ==========================================
// 5. NODE ISOLATION MODAL
// ==========================================
function openIsolateModal(nodeId, nodeName) {
    pendingActionNodeId = nodeId;
    const modal = document.getElementById('node-modal');
    document.getElementById('modal-title').textContent = `Isolate Node (${nodeName})`;
    document.getElementById('modal-desc').textContent = `Are you sure you want to administratively quarantine node ${nodeId}? It will be removed from all routing tables.`;
    modal.classList.remove('hidden');
}

document.getElementById('modal-cancel-btn')?.addEventListener('click', () => {
    document.getElementById('node-modal').classList.add('hidden');
    pendingActionNodeId = null;
});

document.getElementById('modal-confirm-btn')?.addEventListener('click', async () => {
    if (!pendingActionNodeId) return;
    const reason = document.getElementById('modal-reason-input').value || 'Operator quarantine';

    try {
        const res = await fetch(`/api/v1/admin/nodes/${pendingActionNodeId}/isolate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reason })
        });
        if (res.ok) {
            document.getElementById('node-modal').classList.add('hidden');
            fetchAllTelemetry();
        }
    } catch (e) {
        alert('Failed to isolate node: ' + e.message);
    }
});

async function restoreNode(nodeId) {
    try {
        const res = await fetch(`/api/v1/admin/nodes/${nodeId}/restore`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reason: 'Operator restoration' })
        });
        if (res.ok) fetchAllTelemetry();
    } catch (e) {
        alert('Failed to restore node: ' + e.message);
    }
}

// ==========================================
// 6. SIMULATION BENCHMARK RUNNER
// ==========================================
function initSimulationEngine() {
    const runBtn = document.getElementById('run-sim-btn');
    if (!runBtn) return;

    runBtn.addEventListener('click', async () => {
        const scenario = document.getElementById('sim-scenario-select').value;
        const nodeCount = parseInt(document.getElementById('sim-node-count').value, 10) || 25;
        const randomSeed = parseInt(document.getElementById('sim-seed').value, 10) || 849217;

        runBtn.disabled = true;
        runBtn.textContent = 'EXECUTING BENCHMARK...';

        try {
            const res = await fetch('/api/v1/admin/simulations/run', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ scenario, nodeCount, randomSeed })
            });

            if (res.ok) {
                const report = await res.json();
                const resBox = document.getElementById('sim-result-box');
                resBox.classList.remove('hidden');

                document.getElementById('sim-res-scenario').textContent = `${report.scenarioName} (Seed: ${report.randomSeed})`;
                document.getElementById('sim-res-delivery').textContent = `${report.deliveryRatePercent.toFixed(1)}%`;
                document.getElementById('sim-res-packets').textContent = `${report.totalPacketsDelivered} / ${report.totalPacketsSent}`;
                document.getElementById('sim-res-latency').textContent = `${report.averageLatencyMs.toFixed(1)}ms`;
                document.getElementById('sim-res-hops').textContent = `${report.averageHops.toFixed(1)}`;
                fetchAudit();
            }
        } catch (e) {
            alert('Simulation execution failed: ' + e.message);
        } finally {
            runBtn.disabled = false;
            runBtn.textContent = 'EXECUTE BENCHMARK';
        }
    });
}

// ==========================================
// 7. EMERGENCY CONTROLS
// ==========================================
function initEmergencyControls() {
    const dangerBtns = document.querySelectorAll('[data-emergency]');
    dangerBtns.forEach(btn => {
        btn.addEventListener('click', async () => {
            const action = btn.getAttribute('data-emergency');
            if (confirm(`CRITICAL WARNING: Are you sure you want to execute emergency action: ${action.toUpperCase()}?`)) {
                try {
                    const res = await fetch('/api/v1/admin/emergency', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ action, reason: 'Operator emergency trigger' })
                    });
                    if (res.ok) {
                        alert(`Emergency action ${action} executed.`);
                        fetchAudit();
                    }
                } catch (e) {
                    alert('Emergency action failed: ' + e.message);
                }
            }
        });
    });
}

// ==========================================
// 8. SEARCH FILTERS & UTILS
// ==========================================
function initSearchFilters() {
    const search = document.getElementById('node-search');
    if (!search) return;
    search.addEventListener('input', () => {
        const q = search.value.toLowerCase();
        const rows = document.querySelectorAll('#nodes-table-body tr');
        rows.forEach(r => {
            r.style.display = r.textContent.toLowerCase().includes(q) ? '' : 'none';
        });
    });
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
