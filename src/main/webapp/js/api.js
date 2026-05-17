// ═══════════════════════════════════════════════════════
// API MODULE — HTTP client for backend REST APIs
// ═══════════════════════════════════════════════════════

const API_BASE = '';  // Same origin, no prefix needed

/**
 * Generic GET request with auth token.
 */
async function apiGet(url) {
    const token = await getAuthToken();
    const resp = await fetch(API_BASE + url, {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        }
    });
    if (!resp.ok) {
        const err = await resp.json().catch(() => ({ error: 'Request failed' }));
        throw new Error(err.error || 'Request failed');
    }
    return resp.json();
}

/**
 * Generic POST request with auth token.
 */
async function apiPost(url, data) {
    const token = await getAuthToken();
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = 'Bearer ' + token;
    
    const resp = await fetch(API_BASE + url, {
        method: 'POST',
        headers,
        body: JSON.stringify(data)
    });
    if (!resp.ok) {
        const err = await resp.json().catch(() => ({ error: 'Request failed' }));
        throw new Error(err.error || 'Request failed');
    }
    return resp.json();
}

/**
 * Generic PUT request with auth token.
 */
async function apiPut(url, data) {
    const token = await getAuthToken();
    const resp = await fetch(API_BASE + url, {
        method: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });
    if (!resp.ok) {
        const err = await resp.json().catch(() => ({ error: 'Request failed' }));
        throw new Error(err.error || 'Request failed');
    }
    return resp.json();
}

/**
 * Generic DELETE request with auth token.
 */
async function apiDelete(url) {
    const token = await getAuthToken();
    const resp = await fetch(API_BASE + url, {
        method: 'DELETE',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        }
    });
    if (!resp.ok) {
        const err = await resp.json().catch(() => ({ error: 'Request failed' }));
        throw new Error(err.error || 'Request failed');
    }
    return resp.json();
}
