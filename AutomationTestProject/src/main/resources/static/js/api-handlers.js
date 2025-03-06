/**
 * API Handlers
 * Manages API calls and responses for different API flows
 */

// Finicity API Flow Handler
const finicityApiHandler = {
    // Execute the Finicity API flow
    executeFlow: async function() {
        updateStatus('running', 'Executing Finicity API flow...');
        addLogEntry('Starting Finicity API sequence...', 'info');
        
        try {
            // Step 1: Generate Token
            addLogEntry('Step 1: Generating authentication token...', 'info');
            const tokenResponse = await fetch('/api/finicity/token');
            if (!tokenResponse.ok) throw new Error('Failed to generate token');
            const tokenData = await tokenResponse.json();
            addLogEntry('✅ Token generated successfully', 'success');
            
            // Step 2: Create Customer
            addLogEntry('Step 2: Creating customer...', 'info');
            const customerResponse = await fetch('/api/finicity/customer');
            if (!customerResponse.ok) throw new Error('Failed to create customer');
            const customerData = await customerResponse.json();
            addLogEntry(`✅ Customer created successfully. ID: ${customerData.customerId}`, 'success');
            
            // Step 3: Generate Connect Link
            addLogEntry('Step 3: Generating connect link...', 'info');
            const connectLinkResponse = await fetch('/api/finicity/connect');
            if (!connectLinkResponse.ok) throw new Error('Failed to generate connect link');
            const connectLinkData = await connectLinkResponse.json();
            addLogEntry('✅ Connect link generated successfully', 'success');
            
            // Step 4: Process Connect Link with Selenium
            addLogEntry('Step 4: Processing connect link with Selenium...', 'info');
            const seleniumResponse = await fetch('/api/finicity/selenium');
            if (!seleniumResponse.ok) throw new Error('Failed to process connect link');
            const seleniumData = await seleniumResponse.text();
            addLogEntry(seleniumData, 'success');
            
            // Step 5: Refresh Accounts
            addLogEntry('Step 5: Refreshing accounts...', 'info');
            const refreshResponse = await fetch('/api/finicity/refresh');
            if (!refreshResponse.ok) throw new Error('Failed to refresh accounts');
            const refreshData = await refreshResponse.json();
            
            // Display the response data
            document.getElementById('responseDataSection').style.display = 'block';
            document.getElementById('responseData').textContent = JSON.stringify(refreshData, null, 2);
            
            addLogEntry('✅ Accounts refreshed successfully', 'success');
            updateStatus('success', 'API flow completed successfully');
            
        } catch (error) {
            console.error('API Error:', error);
            addLogEntry(`❌ Error: ${error.message}`, 'error');
            updateStatus('error', 'API flow failed');
        }
    }
};

// BIN Lookup API Flow Handler (Placeholder for future implementation)
const binLookupApiHandler = {
    executeFlow: async function() {
        updateStatus('running', 'Executing BIN Lookup API flow...');
        addLogEntry('BIN Lookup API flow is not yet implemented.', 'info');
        
        // This will be implemented in the future
        setTimeout(() => {
            addLogEntry('This feature is coming soon.', 'info');
            updateStatus('idle', 'Ready to execute API calls');
        }, 2000);
    }
};

// Helper function to get the appropriate API handler based on the flow type
function getApiHandler(flowType) {
    switch(flowType) {
        case 'finicity':
            return finicityApiHandler;
        case 'binlookup':
            return binLookupApiHandler;
        default:
            throw new Error(`Unknown API flow type: ${flowType}`);
    }
}
