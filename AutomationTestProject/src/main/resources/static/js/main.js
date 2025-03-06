/**
 * Main.js
 * Core functionality for the API Automation application
 */

// Global variables
let isRunning = false;
let currentApiFlow = null;

// DOM elements
const logsContainer = document.getElementById('logsContainer');
const executeButton = document.getElementById('executeButton');
const statusDot = document.getElementById('statusDot');
const statusText = document.getElementById('statusText');
const responseDataSection = document.getElementById('responseDataSection');
const responseData = document.getElementById('responseData');

// Select an API flow
function selectApiFlow(flowType) {
    // Hide all API sections
    document.querySelectorAll('.api-section').forEach(section => {
        section.style.display = 'none';
    });
    
    // Show the selected API section
    const sectionId = flowType + 'Section';
    const section = document.getElementById(sectionId);
    if (section) {
        section.style.display = 'block';
        currentApiFlow = flowType;
        
        // Reset UI
        resetUI();
    }
}

// Execute API calls for the selected flow
async function executeApiCalls() {
    if (isRunning || !currentApiFlow) return;
    
    isRunning = true;
    executeButton.disabled = true;
    
    try {
        // Get the appropriate API handler for the current flow
        const apiHandler = getApiHandler(currentApiFlow);
        
        // Execute the flow
        await apiHandler.executeFlow();
    } catch (error) {
        console.error('Error executing API flow:', error);
        addLogEntry(`❌ Error: ${error.message}`, 'error');
        updateStatus('error', 'API flow failed');
    } finally {
        isRunning = false;
        executeButton.disabled = false;
    }
}

// Add a log entry to the logs container
function addLogEntry(message, type = 'info') {
    const logEntry = document.createElement('div');
    logEntry.className = `log-entry ${type}`;
    logEntry.textContent = message;
    
    logsContainer.appendChild(logEntry);
    logsContainer.scrollTop = logsContainer.scrollHeight;
}

// Update the status indicator
function updateStatus(status, message) {
    statusDot.className = `status-dot ${status}`;
    statusText.textContent = message;
}

// Reset the UI
function resetUI() {
    // Clear logs
    logsContainer.innerHTML = '<div class="log-entry info">System ready. Click the button above to start the API call sequence.</div>';
    
    // Reset status
    updateStatus('idle', 'Ready to execute API calls');
    
    // Hide response data
    responseDataSection.style.display = 'none';
    responseData.textContent = 'No data available yet. Run the API sequence first.';
    
    // Enable execute button
    executeButton.disabled = false;
    
    // Reset running state
    isRunning = false;
}
