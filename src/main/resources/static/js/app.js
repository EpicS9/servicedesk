/**
 * IT Service Desk — Client Application
 * Enterprise Incident & Support Management
 * Supports Desktop, Tablet, and Mobile devices
 */

let allUsers = [];
let assignableUsers = [];
let allTicketsCache = [];
let currentTicketId = null;
let activeQueueFilter = 'ALL';

// Initialize application on load
async function initApp() {
    setupNavLinks();
    
    // Load all primary data in parallel with error resilience
    await Promise.allSettled([
        loadUsers(),
        loadTickets(),
        loadDashboard(),
        loadBugs()
    ]);
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initApp);
} else {
    initApp();
}

// 1. Navigation Links (Sync desktop and mobile nav bars)
function setupNavLinks() {
    const allNavButtons = document.querySelectorAll('.nav-link');
    allNavButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-tab');

            // Sync active state across all nav buttons (both desktop and mobile)
            allNavButtons.forEach(b => {
                if (b.getAttribute('data-tab') === targetId) {
                    b.classList.add('active');
                } else {
                    b.classList.remove('active');
                }
            });

            // Switch tab view
            document.querySelectorAll('.tab-content').forEach(p => p.classList.remove('active'));
            const targetPane = document.getElementById(targetId);
            if (targetPane) {
                targetPane.classList.add('active');
            }

            if (targetId === 'analytics-tab') loadDashboard();
            if (targetId === 'queue-tab') loadTickets();
            if (targetId === 'bugs-tab') loadBugs();
        });
    });
}

// 2. User & Persona Management
async function loadUsers() {
    try {
        const res = await fetch('/api/users');
        if (!res.ok) throw new Error('Users API returned ' + res.status);
        allUsers = await res.json();

        // Populate both desktop and mobile user selects
        const userSelects = [
            document.getElementById('active-user-select'),
            document.getElementById('active-user-select-mobile')
        ];

        userSelects.forEach(selectEl => {
            if (selectEl && Array.isArray(allUsers)) {
                selectEl.innerHTML = '';
                allUsers.forEach(u => {
                    const opt = document.createElement('option');
                    opt.value = u.id;
                    opt.textContent = `${u.name} (${formatRole(u.role)})`;
                    selectEl.appendChild(opt);
                });

                const defaultUser = allUsers.find(u => u.role === 'SUPPORT_ENGINEER') || allUsers[0];
                if (defaultUser) {
                    selectEl.value = defaultUser.id;
                }
            }
        });

        // Fetch assignable users
        const assignRes = await fetch('/api/users/assignable');
        if (assignRes.ok) {
            assignableUsers = await assignRes.json();
        }

        // Populate Developer dropdown in bug modal
        const devSelect = document.getElementById('bug-dev-select');
        if (devSelect && Array.isArray(allUsers)) {
            devSelect.innerHTML = '<option value="">-- Unassigned --</option>';
            allUsers.filter(u => u.role === 'DEVELOPER').forEach(dev => {
                const opt = document.createElement('option');
                opt.value = dev.id;
                opt.textContent = `${dev.name} (${dev.team || 'Engineering'})`;
                devSelect.appendChild(opt);
            });
        }
    } catch (err) {
        console.error('Failed to load users', err);
    }
}

function handleUserChange(e) {
    const selectedId = parseInt(e.target.value);
    
    // Sync both selects
    const desktopSel = document.getElementById('active-user-select');
    const mobileSel = document.getElementById('active-user-select-mobile');
    if (desktopSel) desktopSel.value = selectedId;
    if (mobileSel) mobileSel.value = selectedId;

    const user = allUsers.find(u => u.id === selectedId);
    if (user) {
        showToast(`Acting as ${user.name} (${formatRole(user.role)})`);
        if (activeQueueFilter === 'ASSIGNED_TO_ME') {
            loadTickets();
        }
    }
}

function getActiveUserId() {
    const desktopSel = document.getElementById('active-user-select');
    const mobileSel = document.getElementById('active-user-select-mobile');
    if (desktopSel && desktopSel.value) return desktopSel.value;
    if (mobileSel && mobileSel.value) return mobileSel.value;
    return '1';
}

function formatRole(roleStr) {
    if (!roleStr) return '';
    return roleStr.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
}

// 3. Incident Queue & Table
async function loadTickets() {
    const statusEl = document.getElementById('filter-status');
    const priorityEl = document.getElementById('filter-priority');
    const desktopSearch = document.getElementById('global-search');
    const mobileSearch = document.getElementById('global-search-mobile');
    const countLabel = document.getElementById('ticket-count-label');
    const tbody = document.getElementById('tickets-table-body');

    const status = statusEl ? statusEl.value : '';
    const priority = priorityEl ? priorityEl.value : '';
    
    // Check either desktop or mobile search query
    let search = '';
    if (desktopSearch && desktopSearch.value) search = desktopSearch.value.trim();
    if (!search && mobileSearch && mobileSearch.value) search = mobileSearch.value.trim();

    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (priority) params.append('priority', priority);
    if (search) params.append('search', search);

    try {
        const queryString = params.toString() ? `?${params.toString()}` : '';
        const res = await fetch(`/api/tickets${queryString}`, {
            headers: { 'Accept': 'application/json' }
        });

        if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        
        let tickets = await res.json();
        if (!Array.isArray(tickets)) {
            throw new Error('Invalid JSON response format from server');
        }
        allTicketsCache = tickets;

        const activeUserId = parseInt(getActiveUserId());

        // Apply View Filter
        if (activeQueueFilter === 'UNASSIGNED') {
            tickets = tickets.filter(t => !t.assignedToId);
        } else if (activeQueueFilter === 'ASSIGNED_TO_ME') {
            tickets = tickets.filter(t => t.assignedToId === activeUserId);
        } else if (activeQueueFilter === 'OPEN_ISSUES') {
            tickets = tickets.filter(t => t.status === 'OPEN' || t.status === 'IN_PROGRESS' || t.status === 'WAITING_FOR_USER');
        } else if (activeQueueFilter === 'CRITICAL_HIGH') {
            tickets = tickets.filter(t => t.priority === 'CRITICAL' || t.priority === 'HIGH');
        } else if (activeQueueFilter === 'RESOLVED') {
            tickets = tickets.filter(t => t.status === 'RESOLVED' || t.status === 'CLOSED');
        }

        if (countLabel) {
            countLabel.textContent = `${tickets.length} ticket${tickets.length !== 1 ? 's' : ''}`;
        }

        if (!tbody) return;
        tbody.innerHTML = '';

        if (tickets.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; padding:30px; color:#5e6c84;">No tickets found matching current filters.</td></tr>';
            return;
        }

        tickets.forEach(t => {
            const tr = document.createElement('tr');
            tr.onclick = () => openTicketDetail(t.id);

            tr.innerHTML = `
                <td><span class="ticket-key">${escapeHtml(t.ticketNumber)}</span></td>
                <td><span class="badge-priority p-${t.priority}">${escapeHtml(t.priority)}</span></td>
                <td>
                    <span style="font-weight:500; color:#172b4d;">${escapeHtml(t.title)}</span>
                    ${t.bugReport ? `<span class="sub-tag">Bug: ${escapeHtml(t.bugReport.bugKey)}</span>` : ''}
                    ${t.resolutionLog ? `<span class="sub-tag">RCA Documented</span>` : ''}
                </td>
                <td>${formatRole(t.category)}</td>
                <td>${escapeHtml(t.creatorName || 'Employee')}</td>
                <td>${t.assignedToName ? escapeHtml(t.assignedToName) : '<span style="color:#8993a4; font-style:italic;">Unassigned</span>'}</td>
                <td><span class="badge-status status-${t.status}">${formatRole(t.status)}</span></td>
                <td style="color:#5e6c84; font-size:12px;">${formatDate(t.createdAt)}</td>
                <td>
                    <button class="btn btn-default btn-sm" onclick="event.stopPropagation(); openTicketDetail(${t.id})">View</button>
                </td>
            `;
            tbody.appendChild(tr);
        });

    } catch (err) {
        console.error('Failed to load tickets:', err);
        if (countLabel) countLabel.textContent = 'Error loading queue';
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" style="text-align:center; padding:24px; color:#de350b;">
                        <div>Failed to fetch tickets: ${escapeHtml(err.message)}</div>
                        <button class="btn btn-default btn-sm" style="margin-top:8px;" onclick="loadTickets()">Retry</button>
                    </td>
                </tr>`;
        }
    }
}

function applyQueueView(viewVal) {
    activeQueueFilter = viewVal;
    loadTickets();
}

let searchTimeout = null;
function handleSearch() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        loadTickets();
    }, 250);
}

function handleMobileSearch(e) {
    const desktopSearch = document.getElementById('global-search');
    if (desktopSearch) desktopSearch.value = e.target.value;
    handleSearch();
}

// 4. Reports & Workload Tab
async function loadDashboard() {
    try {
        const res = await fetch('/api/reports/analytics', {
            headers: { 'Accept': 'application/json' }
        });
        if (!res.ok) throw new Error('Analytics API returned ' + res.status);
        const data = await res.json();

        setElText('kpi-total', data.totalTickets);
        setElText('kpi-open', data.openTickets);
        setElText('kpi-inprogress', data.inProgressTickets);
        setElText('kpi-critical', data.criticalTickets);
        setElText('kpi-resolution-time', `${data.avgResolutionTimeHours}h`);
        setElText('kpi-bugs', data.totalBugs);

        renderWorkloadTable(data.engineerWorkload, data.openTickets + data.inProgressTickets);
        renderPriorityTable(data.ticketsByPriority, data.totalTickets);
        renderCategoryTable(data.ticketsByCategory, data.totalTickets);

    } catch (err) {
        console.error('Failed to load analytics', err);
    }
}

function setElText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text !== undefined ? text : '0';
}

function renderWorkloadTable(workloadMap, totalActive) {
    const tbody = document.getElementById('report-workload-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!workloadMap || Object.keys(workloadMap).length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align:center; color:#5e6c84; padding:12px;">No active workload recorded.</td></tr>';
        return;
    }

    const maxActive = Math.max(...Object.values(workloadMap), 1);

    for (const [engineer, count] of Object.entries(workloadMap)) {
        const pct = Math.round((count / maxActive) * 100);
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${escapeHtml(engineer)}</strong></td>
            <td>${count} ticket${count > 1 ? 's' : ''}</td>
            <td>
                <div class="capacity-bar-bg">
                    <div class="capacity-bar-fill" style="width: ${pct}%;"></div>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    }
}

function renderPriorityTable(priorityMap, total) {
    const tbody = document.getElementById('report-priority-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!priorityMap || Object.keys(priorityMap).length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align:center; color:#5e6c84; padding:12px;">No priority data recorded.</td></tr>';
        return;
    }

    for (const [priority, count] of Object.entries(priorityMap)) {
        const sharePct = total > 0 ? Math.round((count / total) * 100) : 0;
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><span class="badge-priority p-${priority}">${escapeHtml(priority)}</span></td>
            <td>${count}</td>
            <td>${sharePct}% of total</td>
        `;
        tbody.appendChild(tr);
    }
}

function renderCategoryTable(categoryMap, total) {
    const tbody = document.getElementById('report-category-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!categoryMap || Object.keys(categoryMap).length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align:center; color:#5e6c84; padding:12px;">No category data recorded.</td></tr>';
        return;
    }

    for (const [category, count] of Object.entries(categoryMap)) {
        const sharePct = total > 0 ? Math.round((count / total) * 100) : 0;
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${formatRole(category)}</td>
            <td>${count}</td>
            <td>${sharePct}% of total</td>
        `;
        tbody.appendChild(tr);
    }
}

// 5. Ticket Detail View
async function openTicketDetail(id) {
    currentTicketId = id;
    try {
        const res = await fetch(`/api/tickets/${id}`, {
            headers: { 'Accept': 'application/json' }
        });
        if (!res.ok) throw new Error('Ticket detail returned ' + res.status);
        const ticket = await res.json();

        setElText('detail-number', ticket.ticketNumber);
        
        const priorityBadge = document.getElementById('detail-priority-badge');
        if (priorityBadge) {
            priorityBadge.textContent = ticket.priority;
            priorityBadge.className = `badge-priority p-${ticket.priority}`;
        }

        const statusPill = document.getElementById('detail-status-pill');
        if (statusPill) {
            statusPill.textContent = formatRole(ticket.status);
            statusPill.className = `badge-status status-${ticket.status}`;
        }

        setElText('detail-title', ticket.title);
        setElText('detail-description', ticket.description);
        setElText('detail-category', formatRole(ticket.category));
        setElText('detail-requester', `${ticket.creatorName || 'Requester'} (${formatRole(ticket.creatorRole)})`);
        setElText('detail-created', formatDate(ticket.createdAt));

        // Status Select
        const statusSelect = document.getElementById('detail-status-select');
        if (statusSelect) statusSelect.value = ticket.status;

        // Assign Select
        const assignSelect = document.getElementById('detail-assign-select');
        if (assignSelect) {
            assignSelect.innerHTML = '<option value="">-- Unassigned --</option>';
            if (Array.isArray(assignableUsers)) {
                assignableUsers.forEach(u => {
                    const opt = document.createElement('option');
                    opt.value = u.id;
                    opt.textContent = `${u.name} (${formatRole(u.role)})`;
                    if (ticket.assignedToId === u.id) opt.selected = true;
                    assignSelect.appendChild(opt);
                });
            }
        }

        // Resolution RCA Box
        const resBox = document.getElementById('detail-resolution-box');
        if (resBox) {
            if (ticket.resolutionLog) {
                resBox.classList.remove('hidden');
                setElText('detail-res-loggedby', `Documented by ${ticket.resolutionLog.loggedByName || 'Engineer'} on ${formatDate(ticket.resolutionLog.loggedAt)}`);
                setElText('detail-res-problem', ticket.resolutionLog.problemSummary);
                setElText('detail-res-steps', ticket.resolutionLog.investigationSteps);
                setElText('detail-res-rootcause', ticket.resolutionLog.rootCause);
                setElText('detail-res-applied', ticket.resolutionLog.resolutionApplied);
            } else {
                resBox.classList.add('hidden');
            }
        }

        // QA Bug Box
        const bugBox = document.getElementById('detail-bug-box');
        if (bugBox) {
            if (ticket.bugReport) {
                bugBox.classList.remove('hidden');
                setElText('detail-bug-key', ticket.bugReport.bugKey);
                
                const sevBadge = document.getElementById('detail-bug-severity');
                if (sevBadge) {
                    sevBadge.textContent = ticket.bugReport.severity;
                    sevBadge.className = `badge-priority p-${ticket.bugReport.severity}`;
                }

                setElText('detail-bug-steps', ticket.bugReport.stepsToReproduce);
                setElText('detail-bug-expected', ticket.bugReport.expectedBehavior);
                setElText('detail-bug-actual', ticket.bugReport.actualBehavior);
            } else {
                bugBox.classList.add('hidden');
            }
        }

        // Comments
        const commentsList = document.getElementById('detail-comments-list');
        if (commentsList) {
            commentsList.innerHTML = '';
            if (Array.isArray(ticket.comments) && ticket.comments.length > 0) {
                ticket.comments.forEach(c => {
                    const card = document.createElement('div');
                    card.className = `comment-card ${c.internal ? 'internal' : ''}`;
                    card.innerHTML = `
                        <div class="comment-header">
                            <strong>${escapeHtml(c.authorName)} (${formatRole(c.authorRole)})</strong>
                            <span>${c.internal ? '🔒 Internal note • ' : ''}${formatDate(c.createdAt)}</span>
                        </div>
                        <div>${escapeHtml(c.message)}</div>
                    `;
                    commentsList.appendChild(card);
                });
            } else {
                commentsList.innerHTML = '<div style="font-size:12px; color:#5e6c84; padding:4px 0;">No comments recorded on this ticket.</div>';
            }
        }

        // History
        const historyList = document.getElementById('detail-history-list');
        if (historyList) {
            historyList.innerHTML = '';
            if (Array.isArray(ticket.history) && ticket.history.length > 0) {
                ticket.history.forEach(h => {
                    const entry = document.createElement('div');
                    entry.style.padding = '3px 0';
                    entry.innerHTML = `
                        <span>• <strong>${escapeHtml(h.changedByName || 'System')}</strong> changed <em>${escapeHtml(h.fieldChanged)}</em>: <span style="color:#5e6c84;">${escapeHtml(h.oldValue || 'none')}</span> &rarr; <strong>${escapeHtml(h.newValue || 'none')}</strong> (${formatDate(h.timestamp)})</span>
                    `;
                    historyList.appendChild(entry);
                });
            }
        }

        openModal('ticket-detail-modal');
    } catch (err) {
        showToast('Error loading ticket details: ' + err.message);
    }
}

// 6. Create Ticket
function openNewTicketModal() {
    const form = document.getElementById('new-ticket-form');
    if (form) form.reset();
    openModal('new-ticket-modal');
}

async function handleCreateTicket(e) {
    e.preventDefault();
    const payload = {
        title: document.getElementById('new-title').value,
        priority: document.getElementById('new-priority').value,
        category: document.getElementById('new-category').value,
        description: document.getElementById('new-description').value,
        creatorId: parseInt(getActiveUserId())
    };

    try {
        const res = await fetch('/api/tickets', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to create ticket');
        }

        const data = await res.json();
        closeModal('new-ticket-modal');
        showToast(`Ticket ${data.ticketNumber} created successfully`);
        loadTickets();
        loadDashboard();
    } catch (err) {
        showToast(err.message);
    }
}

// 7. Status & Assignment
async function handleStatusChange(e) {
    const newStatus = e.target.value;
    try {
        const res = await fetch(`/api/tickets/${currentTicketId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                status: newStatus,
                userId: parseInt(getActiveUserId())
            })
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to update status');
        }

        showToast(`Status updated to ${formatRole(newStatus)}`);
        openTicketDetail(currentTicketId);
        loadTickets();
        loadDashboard();
    } catch (err) {
        showToast(err.message);
        openTicketDetail(currentTicketId);
    }
}

async function handleAssignChange(e) {
    const assignedId = e.target.value ? parseInt(e.target.value) : null;
    try {
        const res = await fetch(`/api/tickets/${currentTicketId}/assign`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                assignedToUserId: assignedId,
                actionUserId: parseInt(getActiveUserId())
            })
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to assign ticket');
        }

        showToast('Ticket assignment updated');
        openTicketDetail(currentTicketId);
        loadTickets();
        loadDashboard();
    } catch (err) {
        showToast(err.message);
        openTicketDetail(currentTicketId);
    }
}

// 8. Comments
async function handleAddComment(e) {
    e.preventDefault();
    const msg = document.getElementById('comment-message').value;
    const isInternal = document.getElementById('comment-internal').checked;

    try {
        const res = await fetch(`/api/tickets/${currentTicketId}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                authorId: parseInt(getActiveUserId()),
                message: msg,
                isInternal: isInternal
            })
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to post comment');
        }

        document.getElementById('comment-message').value = '';
        document.getElementById('comment-internal').checked = false;
        openTicketDetail(currentTicketId);
    } catch (err) {
        showToast(err.message);
    }
}

// 9. Troubleshooting & RCA
function openTroubleshootModal() {
    const form = document.getElementById('troubleshoot-form');
    if (form) form.reset();
    openModal('troubleshoot-modal');
}

async function handleDocumentResolution(e) {
    e.preventDefault();
    const payload = {
        problemSummary: document.getElementById('ts-problem').value,
        investigationSteps: document.getElementById('ts-steps').value,
        rootCause: document.getElementById('ts-rootcause').value,
        resolutionApplied: document.getElementById('ts-resolution').value,
        loggedById: parseInt(getActiveUserId())
    };

    try {
        const res = await fetch(`/api/tickets/${currentTicketId}/troubleshooting`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to document resolution');
        }

        closeModal('troubleshoot-modal');
        showToast('Resolution documented and ticket marked Resolved');
        openTicketDetail(currentTicketId);
        loadTickets();
        loadDashboard();
    } catch (err) {
        showToast(err.message);
    }
}

// 10. QA Bug Escalation
function openConvertBugModal() {
    const form = document.getElementById('convert-bug-form');
    if (form) form.reset();
    openModal('convert-bug-modal');
}

async function handleConvertBug(e) {
    e.preventDefault();
    const devId = document.getElementById('bug-dev-select').value;
    const payload = {
        title: document.getElementById('bug-title').value,
        severity: document.getElementById('bug-severity').value,
        environment: document.getElementById('bug-env').value,
        stepsToReproduce: document.getElementById('bug-steps').value,
        expectedBehavior: document.getElementById('bug-expected').value,
        actualBehavior: document.getElementById('bug-actual').value,
        stackTrace: document.getElementById('bug-stacktrace').value,
        assignedDeveloperId: devId ? parseInt(devId) : null
    };

    try {
        const res = await fetch(`/api/bugs/tickets/${currentTicketId}/convert`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to escalate ticket to bug');
        }

        closeModal('convert-bug-modal');
        showToast('Ticket converted to QA Defect Report');
        openTicketDetail(currentTicketId);
        loadBugs();
        loadDashboard();
    } catch (err) {
        showToast(err.message);
    }
}

// 11. Bug Tracker
async function loadBugs() {
    try {
        const res = await fetch('/api/bugs', {
            headers: { 'Accept': 'application/json' }
        });
        if (!res.ok) throw new Error('Bugs API returned ' + res.status);
        const bugs = await res.json();

        const tbody = document.getElementById('bugs-table-body');
        if (!tbody) return;
        tbody.innerHTML = '';

        if (!Array.isArray(bugs) || bugs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:24px; color:#5e6c84;">No bugs currently reported.</td></tr>';
            return;
        }

        bugs.forEach(b => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><span class="ticket-key">${escapeHtml(b.bugKey)}</span></td>
                <td><span class="badge-priority p-${b.severity}">${escapeHtml(b.severity)}</span></td>
                <td><strong>${escapeHtml(b.title)}</strong></td>
                <td>${escapeHtml(b.environment)}</td>
                <td>${b.assignedDeveloperName ? escapeHtml(b.assignedDeveloperName) : '<span style="color:#8993a4; font-style:italic;">Unassigned</span>'}</td>
                <td><span class="badge-status status-${b.status}">${formatRole(b.status)}</span></td>
                <td style="color:#5e6c84; font-size:12px;">${formatDate(b.createdAt)}</td>
                <td>
                    <select onchange="updateBugStatus(${b.id}, this.value)" class="form-select select-sm">
                        <option value="OPEN" ${b.status === 'OPEN' ? 'selected' : ''}>Open</option>
                        <option value="IN_TRIAGE" ${b.status === 'IN_TRIAGE' ? 'selected' : ''}>In Triage</option>
                        <option value="IN_FIX" ${b.status === 'IN_FIX' ? 'selected' : ''}>In Fix</option>
                        <option value="VERIFIED" ${b.status === 'VERIFIED' ? 'selected' : ''}>Verified</option>
                        <option value="CLOSED" ${b.status === 'CLOSED' ? 'selected' : ''}>Closed</option>
                    </select>
                </td>
            `;
            tbody.appendChild(tr);
        });

    } catch (err) {
        console.error('Failed to load bugs', err);
    }
}

async function updateBugStatus(bugId, status) {
    try {
        const res = await fetch(`/api/bugs/${bugId}/status?status=${status}`, {
            method: 'PUT'
        });
        if (!res.ok) throw new Error('Failed to update bug status');
        showToast(`Bug status updated to ${formatRole(status)}`);
        loadBugs();
        loadDashboard();
    } catch (err) {
        showToast(err.message);
    }
}

// Modal & Toast Utilities
function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('hidden');
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('hidden');
}

function handleBackdropClick(e, id) {
    if (e.target.classList.contains('modal-overlay')) {
        closeModal(id);
    }
}

function showToast(msg) {
    const box = document.getElementById('toast-box');
    if (!box) return;

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = msg;
    box.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.25s ease';
        setTimeout(() => toast.remove(), 250);
    }, 3000);
}

// Date Formatter
function formatDate(isoStr) {
    if (!isoStr) return '-';
    try {
        const clean = String(isoStr).replace(' ', 'T');
        const d = new Date(clean);
        if (!isNaN(d.getTime())) {
            return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) + ' ' +
                   d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        }
        const parts = clean.split('T');
        return parts[0] + (parts[1] ? ' ' + parts[1].substring(0, 5) : '');
    } catch (e) {
        return String(isoStr).substring(0, 16).replace('T', ' ');
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}
