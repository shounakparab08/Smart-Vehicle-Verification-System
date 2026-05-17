// ═══════════════════════════════════════════════════════
// MOCK Firebase Configuration (Bypass)
// ═══════════════════════════════════════════════════════

// Mock Auth Object
const auth = {
    currentUser: null,
    _callbacks: [],
    
    onAuthStateChanged: function(callback) {
        this._callbacks.push(callback);
        const storedUser = localStorage.getItem('mockUser');
        if (storedUser) {
            let user = JSON.parse(storedUser);
            // Migrate old ugly UIDs to use email instead
            if (user.uid && user.uid.startsWith("user-")) {
                user.uid = user.email;
                localStorage.setItem('mockUser', JSON.stringify(user));
            }
            this.currentUser = user;
            this.currentUser.getIdToken = async () => "mock_token_" + this.currentUser.uid;
            this.currentUser.sendEmailVerification = async () => {};
            callback(this.currentUser);
        } else {
            this.currentUser = null;
            callback(null);
        }
    },

    _notify: function() {
        this._callbacks.forEach(cb => cb(this.currentUser));
    },
    
    createUserWithEmailAndPassword: async function(email, password) {
        const uid = email;
        const user = { uid, email };
        localStorage.setItem('mockUser', JSON.stringify(user));
        
        this.currentUser = {
            ...user,
            sendEmailVerification: async () => {},
            getIdToken: async () => "mock_token_" + uid
        };
        
        this._notify();
        return { user: this.currentUser };
    },
    
    signInWithEmailAndPassword: async function(email, password) {
        const uid = email;
        const user = { uid, email };
        localStorage.setItem('mockUser', JSON.stringify(user));
        
        this.currentUser = {
            ...user,
            getIdToken: async () => "mock_token_" + uid
        };
        
        this._notify();
        return { user: this.currentUser };
    },
    
    signOut: async function() {
        localStorage.removeItem('mockUser');
        this.currentUser = null;
        this._notify();
    },
    
    sendPasswordResetEmail: async function(email) {
        return Promise.resolve();
    }
};

console.log("Mock Firebase initialized.");
