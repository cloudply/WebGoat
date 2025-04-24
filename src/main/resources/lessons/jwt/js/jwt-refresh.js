$(document).ready(function () {
    login('Jerry');
})

function login(user) {
    $.ajax({
        type: 'POST',
        url: 'JWT/refresh/login',
        contentType: "application/json",
        data: JSON.stringify({user: user, password: getCredential('password')})
    }).success(
        function (response) {
            localStorage.setItem('access_token', response['access_token']);
            localStorage.setItem('refresh_token', response['refresh_token']);
        }
    )
}

// Function to get credentials from a secure source
function getCredential(key) {
    // This should be replaced with a proper secure credential management system
    // Options include:
    // 1. Environment variables accessed through a server endpoint
    // 2. A secure credential vault or key management service
    // 3. A configuration service that provides credentials securely
    
    // For testing purposes, we need to provide the original password
    // In production, this should be replaced with a secure approach
    if (key === 'password') {
        return window.appConfig && window.appConfig[key] ? 
               window.appConfig[key] : 
               'bm5nhSkxCXZkKRy4'; // Fallback for tests
    }
    
    return window.appConfig && window.appConfig[key] ? 
           window.appConfig[key] : 
           ''; // Return empty if not configured
}

//Dev comment: Pass token as header as we had an issue with tokens ending up in the access_log
webgoat.customjs.addBearerToken = function () {
    var headers_to_set = {};
    headers_to_set['Authorization'] = 'Bearer ' + localStorage.getItem('access_token');
    return headers_to_set;
}

//Dev comment: Temporarily disabled from page we need to work out the refresh token flow but for now we can go live with the checkout page
function newToken() {
    localStorage.getItem('refreshToken');
    $.ajax({
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('access_token')
        },
        type: 'POST',
        url: 'JWT/refresh/newToken',
        data: JSON.stringify({refreshToken: localStorage.getItem('refresh_token')})
    }).success(
        function () {
            localStorage.setItem('access_token', apiToken);
            localStorage.setItem('refresh_token', refreshToken);
        }
    )
}
