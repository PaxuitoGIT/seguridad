// Estado de la aplicación
let credentials = null;
let currentFilter = 'all';

// Inicializar cuando carga la página
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    setupEventListeners();
    showLoginModal();
}

// Configurar event listeners
function setupEventListeners() {
    // Login modal
    document.getElementById('loginBtn').addEventListener('click', showLoginModal);
    document.getElementById('logoutBtn').addEventListener('click', logout);
    document.querySelector('.close').addEventListener('click', closeLoginModal);
    document.getElementById('loginForm').addEventListener('submit', handleLogin);

    // Refresh buttons
    document.getElementById('refreshSensors').addEventListener('click', loadSensors);

    // Filter buttons
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            currentFilter = this.dataset.filter;
            loadEvents();
        });
    });
}

// Login functions
function showLoginModal() {
    document.getElementById('loginModal').style.display = 'block';
}

function closeLoginModal() {
    document.getElementById('loginModal').style.display = 'none';
}

function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('usernameInput').value;
    const password = document.getElementById('passwordInput').value;

    credentials = btoa(`${username}:${password}`);

    document.getElementById('username').innerHTML = `👤 Usuario: <strong>${username}</strong>`;
    document.getElementById('loginBtn').style.display = 'none';
    document.getElementById('logoutBtn').style.display = 'inline-block';

    closeLoginModal();
    showNotification('Login exitoso', 'success');

    // Cargar datos
    loadDashboard();
    loadSensors();
    loadEvents();

    // Auto-refresh cada 5 segundos
    setInterval(() => {
        loadDashboard();
        loadEvents();
    }, 5000);
}

function logout() {
    credentials = null;
    document.getElementById('username').innerHTML = '👤 Usuario: <strong>No autenticado</strong>';
    document.getElementById('loginBtn').style.display = 'inline-block';
    document.getElementById('logoutBtn').style.display = 'none';
    showLoginModal();
}

// API Functions
async function apiCall(endpoint, method = 'GET', body = null) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Basic ${credentials}`
        }
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`/api/${endpoint}`, options);

        // Si la respuesta no es OK, lanzar error
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        // Verificar si hay contenido para parsear
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            return await response.json();
        } else {
            // Si no es JSON, retornar texto
            const text = await response.text();
            console.log('Respuesta no-JSON:', text);
            return { success: true, message: text };
        }
    } catch (error) {
        console.error('Error completo:', error);
        showNotification('Error: ' + error.message, 'error');
        return null;
    }
}

// Load Dashboard Stats
async function loadDashboard() {
    const stats = await apiCall('events/stats');
    if (stats) {
        document.getElementById('totalSensors').textContent = stats.totalSensors;
        document.getElementById('activeSensors').textContent = stats.activeSensors;
        document.getElementById('totalEvents').textContent = stats.totalEvents;
        document.getElementById('criticalEvents').textContent = stats.unprocessedCriticalEvents;
    }
}

// Load Sensors
async function loadSensors() {
    const sensors = await apiCall('sensors');
    if (sensors) {
        const grid = document.getElementById('sensorsGrid');
        grid.innerHTML = sensors.map(sensor => `
            <div class="sensor-card ${sensor.type}">
                <div class="sensor-header">
                    <span class="sensor-title">${sensor.sensorId}</span>
                    <span class="sensor-status ${sensor.active ? 'active' : 'inactive'}">
                        ${sensor.active ? '✓ Activo' : '✗ Inactivo'}
                    </span>
                </div>
                <div class="sensor-location">📍 ${sensor.location}</div>
                <div class="sensor-info">
                    ${sensor.lastCheck ?
            `Última verificación: ${new Date(sensor.lastCheck).toLocaleString('es-ES')}` :
            'No verificado aún'}
                </div>
                <span class="sensor-type-badge ${sensor.type}">
                    ${getSensorIcon(sensor.type)} ${sensor.type}
                </span>
            </div>
        `).join('');

        showNotification('Sensores actualizados', 'success');
    }
}

// Load Events
async function loadEvents() {
    let endpoint = 'events';
    if (currentFilter === 'critical') {
        endpoint = 'events/critical';
    }

    const events = await apiCall(endpoint);
    if (events) {
        let filteredEvents = events;

        // Filtrar por tipo si no es 'all' o 'critical'
        if (currentFilter !== 'all' && currentFilter !== 'critical') {
            filteredEvents = events.filter(e => e.sensor.type === currentFilter);
        }

        const container = document.getElementById('eventsContainer');
        if (filteredEvents.length === 0) {
            container.innerHTML = '<p style="text-align:center; color:#7f8c8d; padding:20px;">No hay eventos para mostrar</p>';
        } else {
            container.innerHTML = filteredEvents.map(event => `
                <div class="event-card ${event.critical ? 'critical' : 'normal'}">
                    <div class="event-header">
                        <span class="event-type">${event.eventType}</span>
                        <span class="event-time">${new Date(event.detectedAt).toLocaleString('es-ES')}</span>
                    </div>
                    <div class="event-description">${event.description}</div>
                    <div>
                        <span class="event-badge ${event.critical ? 'critical' : 'normal'}">
                            ${event.critical ? '🚨 CRÍTICO' : '✓ Normal'}
                        </span>
                        <span class="event-badge" style="background:#3498db; color:white;">
                            ${getSensorIcon(event.sensor.type)} ${event.sensor.type}
                        </span>
                        <span style="color:#7f8c8d; font-size:0.9em;">
                            📍 ${event.sensor.location}
                        </span>
                    </div>
                </div>
            `).join('');
        }
    }
}

// Simulation Functions
async function simulateMovement() {
    const sensorId = document.getElementById('movementSensor').value;
    const data = {
        type: "MOVEMENT",
        data: true
    };

    const result = await apiCall(`sensors/${sensorId}/process`, 'POST', data);

    if (result && result.success) {
        showNotification(`⚠️ Movimiento detectado en ${sensorId}`, 'warning');

        setTimeout(() => {
            loadDashboard();
            loadEvents();
            loadSensors();
        }, 1500);
    }
}

async function simulateTemperature() {
    const sensorId = document.getElementById('tempSensor').value;
    const temp = parseFloat(document.getElementById('tempValue').value);
    const data = {
        type: "TEMPERATURE",
        data: temp
    };

    const result = await apiCall(`sensors/${sensorId}/process`, 'POST', data);

    if (result && result.success) {
        showNotification(
            `🌡️ Temperatura de ${temp}°C detectada en ${sensorId}`,
            temp > 50 ? 'error' : 'success'
        );

        setTimeout(() => {
            loadDashboard();
            loadEvents();
            loadSensors();
        }, 1500);
    }
}

async function simulateAccess() {
    const sensorId = document.getElementById('accessSensor').value;
    const userId = document.getElementById('userId').value;
    const authorized = document.getElementById('authorized').checked;
    const data = {
        type: "ACCESS",
        data: {
            userId: userId,
            authorized: authorized
        }
    };

    const result = await apiCall(`sensors/${sensorId}/process`, 'POST', data);

    if (result && result.success) {
        showNotification(
            `🚪 ${authorized ? 'Acceso autorizado' : 'Acceso denegado'} para ${userId}`,
            authorized ? 'success' : 'error'
        );

        setTimeout(() => {
            loadDashboard();
            loadEvents();
            loadSensors();
        }, 1500);
    }
}

async function simulateConcurrent() {
    const batch = [
        {sensorId: "MOV-001", type: "MOVEMENT", data: true},
        {sensorId: "MOV-002", type: "MOVEMENT", data: true},
        {sensorId: "TEMP-001", type: "TEMPERATURE", data: 90.0},
        {sensorId: "TEMP-002", type: "TEMPERATURE", data: 70.0},
        {sensorId: "ACC-001", type: "ACCESS", data: {userId: "STARK-001", authorized: true}}
    ];

    const result = await apiCall('sensors/process-batch', 'POST', batch);

    if (result && result.success) {
        showNotification('🚀 Procesando 5 sensores simultáneamente...', 'warning');

        setTimeout(() => {
            loadDashboard();
            loadEvents();
            loadSensors();
            showNotification('✅ Procesamiento concurrente completado', 'success');
        }, 2000);
    }
}

// Helper Functions
function getSensorIcon(type) {
    const icons = {
        'MOVEMENT': '🏃',
        'TEMPERATURE': '🌡️',
        'ACCESS': '🚪'
    };
    return icons[type] || '📡';
}

function showNotification(message, type = 'success') {
    const container = document.getElementById('notifications');
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;

    container.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Animación de salida
const style = document.createElement('style');
style.textContent = `
    @keyframes slideOut {
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);