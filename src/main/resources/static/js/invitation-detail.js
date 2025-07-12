

function customFetch(url, options = {}) {
    return fetch(url, { ...options, redirect: 'manual' }) // Prevent auto-redirect
        .then(response => {
            if (response.status === 302 || response.type === 'opaqueredirect') {
                console.warn('Session expired, redirecting to login');
                window.location.href = '/login?expired';
                return Promise.reject(new Error('Session expired'));
            }
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(`Server error: ${response.status} - ${text}`);
                });
            }
            // Check if response is JSON
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                console.warn('Non-JSON response, likely login page, redirecting');
                window.location.href = '/login?expired';
                return Promise.reject(new Error('Invalid response type'));
            }
            return response.json();
        });
}
