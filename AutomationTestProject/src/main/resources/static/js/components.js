/**
 * Components.js
 * Handles loading of HTML components and UI elements
 */

// Load components
async function loadComponents() {
    try {
        const headerResponse = await fetch('components/header.html');
        if (!headerResponse.ok) throw new Error('Failed to load header');
        document.getElementById('header').innerHTML = await headerResponse.text();
        
        const apiFlowsResponse = await fetch('components/apiFlows.html');
        if (!apiFlowsResponse.ok) throw new Error('Failed to load API flows');
        document.getElementById('apiFlows').innerHTML = await apiFlowsResponse.text();
        
        // Initialize event listeners after components are loaded
        initializeEventListeners();
    } catch (error) {
        console.error('Error loading components:', error);
        document.getElementById('header').innerHTML = '<div class="log-entry error">Error loading header</div>';
        document.getElementById('apiFlows').innerHTML = '<div class="log-entry error">Error loading API flows</div>';
    }
}

// Initialize event listeners for components
function initializeEventListeners() {
    // Add event listeners to API flow selection buttons
    const finicityButton = document.querySelector('button[onclick="selectApiFlow(\'finicity\')"]');
    const binLookupButton = document.querySelector('button[onclick="selectApiFlow(\'binlookup\')"]');
    
    if (finicityButton) {
        finicityButton.addEventListener('click', function() {
            selectApiFlow('finicity');
        });
    }
    
    if (binLookupButton) {
        binLookupButton.addEventListener('click', function() {
            selectApiFlow('binlookup');
        });
    }
}

// Load components when the page loads
document.addEventListener('DOMContentLoaded', loadComponents);
