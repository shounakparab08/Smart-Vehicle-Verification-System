// ═══════════════════════════════════════════════════════
// AUTH MODULE — Firebase Authentication Functions
// ═══════════════════════════════════════════════════════

/**
 * Register a new user with email/password + name.
 * After Firebase creates the account, we also register in our backend MongoDB.
 */
async function registerUser(name, email, password) {
    try {
        showLoader();
        const cred = await auth.createUserWithEmailAndPassword(email, password);
        
        // Send email verification
        await cred.user.sendEmailVerification();
        
        // Get token and register in backend
        const token = await cred.user.getIdToken();
        await apiPost('/api/user/register', { token, name, email, role: 'customer' });
        
        hideLoader();
        showToast('Account created! Please verify your email.', 'success');
        return cred.user;
    } catch (error) {
        hideLoader();
        throw error;
    }
}

/**
 * Register a new admin user.
 */
async function registerAdmin(name, email, password) {
    try {
        showLoader();
        const cred = await auth.createUserWithEmailAndPassword(email, password);
        
        await cred.user.sendEmailVerification();
        
        const token = await cred.user.getIdToken();
        await apiPost('/api/user/register', { token, name, email, role: 'admin' });
        
        hideLoader();
        showToast('Admin account created! Please verify your email.', 'success');
        return cred.user;
    } catch (error) {
        hideLoader();
        throw error;
    }
}

/**
 * Login with email and password.
 */
async function loginUser(email, password) {
    try {
        showLoader();
        const cred = await auth.signInWithEmailAndPassword(email, password);
        hideLoader();
        return cred.user;
    } catch (error) {
        hideLoader();
        throw error;
    }
}

/**
 * Send password reset email.
 */
async function forgotPassword(email) {
    try {
        showLoader();
        await auth.sendPasswordResetEmail(email);
        hideLoader();
        showToast('Password reset email sent!', 'success');
    } catch (error) {
        hideLoader();
        throw error;
    }
}

/**
 * Sign out the current user.
 */
async function logoutUser() {
    await auth.signOut();
    window.location.href = '/customer/login.html';
}

/**
 * Sign out admin.
 */
async function logoutAdmin() {
    await auth.signOut();
    window.location.href = '/admin/admin-login.html';
}

/**
 * Get the current user's Firebase ID token for API calls.
 */
async function getAuthToken() {
    const user = auth.currentUser;
    if (!user) return null;
    return await user.getIdToken();
}

/**
 * Check if user is logged in. Redirect to login page if not.
 * Call this on protected pages.
 */
function requireAuth(redirectUrl) {
    return new Promise((resolve) => {
        auth.onAuthStateChanged(async (user) => {
            if (!user) {
                window.location.href = redirectUrl || '/customer/login.html';
            } else {
                // Fetch profile to update UI elements like avatars
                apiGet('/api/user/profile').then(profile => {
                    if (profile && profile.name) updateHeaderAvatar(profile.name);
                }).catch(() => {});
                resolve(user);
            }
        });
    });
}

/**
 * Check if user is admin. Redirect if not.
 */
async function requireAdmin() {
    const user = await requireAuth('/admin/admin-login.html');
    const token = await user.getIdToken();
    try {
        const profile = await apiGet('/api/user/profile');
        if (!profile || profile.role !== 'admin') {
            showToast('Admin access required', 'error');
            auth.signOut();
            window.location.href = '/admin/admin-login.html';
            return null;
        }
        if (profile && profile.name) updateHeaderAvatar(profile.name);
        return profile;
    } catch (e) {
        window.location.href = '/admin/admin-login.html';
        return null;
    }
}

/**
 * Get a friendly error message from Firebase error codes.
 */
function getAuthErrorMessage(error) {
    const messages = {
        'auth/email-already-in-use': 'This email is already registered.',
        'auth/invalid-email': 'Please enter a valid email address.',
        'auth/weak-password': 'Password must be at least 6 characters.',
        'auth/user-not-found': 'No account found with this email.',
        'auth/wrong-password': 'Incorrect password. Please try again.',
        'auth/too-many-requests': 'Too many attempts. Please try again later.',
        'auth/user-disabled': 'This account has been disabled.',
        'auth/network-request-failed': 'Network error. Check your connection.',
    };
    return messages[error.code] || error.message;
}

/**
 * Check if user has at least one registered vehicle.
 * Returns a promise that resolves to true if user has vehicles, false otherwise.
 * Does NOT auto-popup. Pages should call this only when the user tries to use
 * a feature that requires a registered vehicle (e.g. search, report).
 */
async function requireVehicle() {
    try {
        const vehicles = await apiGet('/api/vehicle/my-vehicles');
        if (!vehicles || vehicles.length === 0) {
            // Show a non-blocking toast notification instead of a blocking modal
            showToast('Please register a vehicle first to use this feature.', 'warning');
            return false;
        }
        return true;
    } catch (e) {
        console.error('Vehicle check failed:', e);
        return true; // Don't block if the check itself fails
    }
}
