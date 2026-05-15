document.addEventListener('DOMContentLoaded', () => {
    // 1. Language Detection & Setup
    const savedLang = localStorage.getItem('lang') || navigator.language.split('-')[0] || 'en';
    const langNames = { 
        'en': 'English', 
        'uz': 'O\'zbek', 
        'ru': 'Русский',
        'es': 'Español',
        'zh': '中文',
        'ja': '日本語',
        'ar': 'العربية'
    };
    const langIcons = { 
        'en': '🇺🇸', 
        'uz': '🇺🇿', 
        'ru': '🇷🇺',
        'es': '🇪🇸',
        'zh': '🇨🇳',
        'ja': '🇯🇵',
        'ar': '🇸🇦'
    };
    
    // Set initial UI
    const iconEl = document.getElementById('current-lang-icon');
    const textEl = document.getElementById('current-lang-text');
    if (iconEl && textEl) {
        const finalLang = langNames[savedLang] ? savedLang : 'en';
        iconEl.innerText = langIcons[finalLang];
        textEl.innerText = langNames[finalLang];
    }

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('app_id')) {
        sessionStorage.setItem('app_id', urlParams.get('app_id'));
    }
    if (urlParams.has('redirect_uri')) {
        sessionStorage.setItem('redirect_uri', urlParams.get('redirect_uri'));
    }

    const currentAppId = sessionStorage.getItem('app_id');
    if (currentAppId && currentAppId !== 'AURALIFE') {
        const headerP = document.querySelector('.auth-header p');
        const headerH1 = document.querySelector('.auth-header h1');
        if (headerP) headerP.innerText = `to continue to ${currentAppId}`;
        if (headerH1) headerH1.innerText = `Sign in with Auralife`;
    }

    if (!urlParams.has('lang')) {
        urlParams.set('lang', langNames[savedLang] ? savedLang : 'en');
        window.history.replaceState({}, '', `${window.location.pathname}?${urlParams.toString()}`);
    }

    // 2. Theme Setup
    const themeSwitch = document.getElementById('theme-toggle');
    const authLogo = document.getElementById('auth-logo');
    
    function updateLogo(theme) {
        if (!authLogo) return;
        const logoSrc = theme === 'dark' 
            ? '/assets/auralife-logo-dark-theme.png' 
            : '/assets/auralife-logo-light-theme.png';
        authLogo.src = logoSrc;
    }

    const currentTheme = localStorage.getItem('theme') || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    document.documentElement.setAttribute('data-theme', currentTheme);
    updateLogo(currentTheme);

    if (themeSwitch) {
        themeSwitch.checked = (currentTheme === 'dark');
        themeSwitch.addEventListener('change', function(e) {
            const newTheme = e.target.checked ? 'dark' : 'light';
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
            updateLogo(newTheme);
        });
    }

    // 3. Flow Logic: Redirect to Account Chooser if accounts exist
    const isLoginPage = window.location.pathname.includes('/login');
    const isSignupPage = window.location.pathname.includes('/signup');
    
    // Auto-redirect to Login if no accounts on Chooser page
    if (window.location.pathname.includes('/account-chooser')) {
        const accounts = JSON.parse(localStorage.getItem('savedAccounts')) || [];
        if (accounts.length === 0) {
            window.location.href = '/auth/page/login';
            return;
        }
    }

    if (isLoginPage && !urlParams.has('username')) {
        const accounts = JSON.parse(localStorage.getItem('savedAccounts')) || [];
        const skipChooser = sessionStorage.getItem('skipChooser') === 'true';
        if (accounts.length > 0 && !skipChooser) {
            window.location.href = '/auth/page/account-chooser' + window.location.search;
            return;
        }
    }

    const authContainer = document.querySelector('.auth-container');
    const loadingOverlay = document.getElementById('initial-loading');
    if (authContainer) {
        if (loadingOverlay) loadingOverlay.style.display = 'none';
        authContainer.style.opacity = '1';
    }

    // 4. Form Handlers
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        // Pre-fill username if in URL
        const userParam = urlParams.get('username');
        if (userParam) {
            const emailField = document.getElementById('email');
            if (emailField) emailField.value = userParam;
        }

        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const emailInput = document.getElementById('email').value;
            const passwordInput = document.getElementById('password').value;

            fetch('/auth/signin', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    phoneNumber: emailInput,
                    password: passwordInput,
                    deviceName: getDeviceName(),
                    deviceType: "WEB",
                    app: sessionStorage.getItem('app_id') || "AURALIFE"
                })
            })
            .then(async res => {
                let data = {};
                try { data = await res.json(); } catch(e) {}
                if((res.ok && data.status === 200) || data.accessToken) {
                    const fullName = (data.firstName && data.lastName) ? `${data.firstName} ${data.lastName}` : data.firstName || emailInput.split('@')[0];
                    saveAccount(emailInput, fullName, data.accessToken, data.profilePhotoFileId);
                    localStorage.setItem('accessToken', data.accessToken);
                    const redirectUri = sessionStorage.getItem('redirect_uri');
                    const appId = sessionStorage.getItem('app_id');
                    
                    if (redirectUri && redirectUri !== 'null' && appId && appId !== 'AURALIFE') {
                        const separator = redirectUri.includes('?') ? '&' : '?';
                        window.location.href = redirectUri + separator + "token=" + data.accessToken;
                    } else {
                        window.location.href = '/auth/page/account-chooser';
                    }
                } else {
                    fetch('/auth/signin-byemail', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            email: emailInput,
                            password: passwordInput,
                            deviceName: getDeviceName(),
                            deviceType: "WEB",
                            app: sessionStorage.getItem('app_id') || "AURALIFE"
                        })
                    })
                    .then(async res2 => {
                        let data2 = {};
                        try { data2 = await res2.json(); } catch(e) {}
                        if((res2.ok && data2.status === 200) || data2.accessToken) {
                            const fullName = (data2.firstName && data2.lastName) ? `${data2.firstName} ${data2.lastName}` : data2.firstName || emailInput.split('@')[0];
                            saveAccount(emailInput, fullName, data2.accessToken, data2.profilePhotoFileId);
                            localStorage.setItem('accessToken', data2.accessToken);
                            const redirectUriEmail = sessionStorage.getItem('redirect_uri');
                            const appIdEmail = sessionStorage.getItem('app_id');
                            if (redirectUriEmail && redirectUriEmail !== 'null' && appIdEmail && appIdEmail !== 'AURALIFE') {
                                const separator = redirectUriEmail.includes('?') ? '&' : '?';
                                window.location.href = redirectUriEmail + separator + "token=" + data2.accessToken;
                            } else {
                                window.location.href = '/auth/page/account-chooser';
                            }
                        } else {
                            showToast(data2.message || data2.error || document.querySelector('.error-msg')?.innerText || "Login failed. Check credentials.", 'error');
                        }
                    }).catch(() => showToast("Login request failed. Please try again.", 'error'));
                }
            }).catch(() => showToast("Connection error occurred. Server might be down.", 'error'));
        });
    }

    const signupForm = document.getElementById('signup-form');
    if (signupForm) {
        signupForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const btn = document.getElementById('signup-submit-btn');
            btn.disabled = true;
            btn.innerText = "...";

            const payload = {
                firstName: document.getElementById('firstName').value,
                lastName: document.getElementById('lastName').value,
                username: document.getElementById('username').value,
                email: document.getElementById('email').value,
                phoneNumber: document.getElementById('phoneNumber').value,
                birthDate: document.getElementById('birthDate').value,
                gender: document.getElementById('gender').value,
                password: document.getElementById('password').value,
                deviceName: getDeviceName(),
                deviceType: "WEB",
                app: sessionStorage.getItem('app_id') || "AURALIFE"
            };

            fetch('/auth/signup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(async (res) => {
                const data = await res.json();
                if(res.ok || data.status === 200) {
                    showToast("Registration successful! Check email.", 'success');
                    setTimeout(() => {
                        window.location.href = '/auth/page/activate?email=' + encodeURIComponent(payload.email);
                    }, 1000);
                } else {
                    showToast(data.message || data.error || 'Signup failed', 'error');
                    btn.disabled = false;
                    btn.innerText = "Sign Up";
                }
            })
            .catch((err) => {
                showToast("Connection error occurred.", 'error');
                btn.disabled = false;
                btn.innerText = "Sign Up";
            });
        });
    }
    
    const activateForm = document.getElementById('activate-form');
    if (activateForm) {
        activateForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const code = document.getElementById('code').value;
            fetch('/auth/activate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: email, code: code })
            })
            .then(res => res.json())
            .then(data => {
                if (data && data.status === 200) {
                    showToast("Account verified!", 'success');
                    setTimeout(() => { window.location.href = '/auth/page/login'; }, 1500);
                } else {
                    showToast(data ? data.message : "Verification failed", 'error');
                }
            })
            .catch(err => showToast('Connection error occurred.', 'error'));
        });
    }
});

// Global Helpers
window.skipChooserAndGo = function(target) {
    sessionStorage.setItem('skipChooser', 'true');
    window.location.href = target;
}

window.toggleDropdown = function() {
    const opts = document.getElementById('lang-options');
    if (opts) opts.classList.toggle('show');
}

window.toggleGenderDropdown = function() {
    const opts = document.getElementById('gender-options');
    if (opts) opts.classList.toggle('show');
}

window.selectGender = function(val, label) {
    const input = document.getElementById('gender');
    const text = document.getElementById('current-gender-text');
    if (input) input.value = val;
    if (text) text.innerText = label;
    const opts = document.getElementById('gender-options');
    if (opts) opts.classList.remove('show');
}

// Close dropdowns when clicking outside
document.addEventListener('click', (e) => {
    if (!e.target.closest('#lang-dropdown')) {
        const opts = document.getElementById('lang-options');
        if (opts) opts.classList.remove('show');
    }
    if (!e.target.closest('#gender-dropdown')) {
        const opts = document.getElementById('gender-options');
        if (opts) opts.classList.remove('show');
    }
});

// Google Custom Button Click
document.addEventListener('DOMContentLoaded', () => {
    const googleLoginBtn = document.getElementById('google-login-btn');
    const googleSignupBtn = document.getElementById('google-signup-btn');
    
    if (googleLoginBtn || googleSignupBtn) {
        const btn = googleLoginBtn || googleSignupBtn;
        btn.onclick = () => {
             // This triggers the one tap prompt if configured, or we can use the library to show the selector
             google.accounts.id.prompt();
        };
    }
});

window.changeLanguage = function(lang) {
    localStorage.setItem('lang', lang);
    const url = new URL(window.location.href);
    url.searchParams.set('lang', lang);
    window.location.href = url.toString();
}

window.checkEmailExists = function(email) {
    if (!email || !email.includes('@')) return;
    fetch('/auth/checkup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email })
    })
    .then(res => res.json())
    .then(data => {
        if (data.email) {
            showToast("This email is already registered.", 'error');
            const emailField = document.getElementById('email');
            if(emailField) emailField.style.borderColor = 'rgba(255, 77, 79, 0.5)';
        } else {
            const emailField = document.getElementById('email');
            if(emailField) emailField.style.borderColor = 'rgba(82, 196, 26, 0.5)';
        }
    });
}

// Define globally as early as possible to avoid race conditions with GIS
window.handleCredentialResponse = function(response) {
    console.log("GIS callback triggered with response:", response);
    if(!response.credential) {
        console.error("No credential found in GIS response");
        return;
    }
    const base64Url = response.credential.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    const googleUser = JSON.parse(jsonPayload);
    console.log("Decoded Google payload:", googleUser);
    const trustDto = {
        email: googleUser.email,
        firstName: googleUser.given_name || "",
        lastName: googleUser.family_name || "",
        deviceName: getDeviceName(),
        deviceType: "WEB",
        app: sessionStorage.getItem('app_id') || "AURALIFE"
    };

    fetch('/auth/google-trusted', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(trustDto)
    })
    .then(res => res.json())
    .then(data => {
        if (data && (data.status === 200 || data.accessToken)) {
            showToast("Google login successful!", 'success');
            saveAccount(googleUser.email, googleUser.given_name || googleUser.email.split('@')[0], data.accessToken);
            localStorage.setItem('accessToken', data.accessToken);
            const redirectUri = sessionStorage.getItem('redirect_uri');
            const appId = sessionStorage.getItem('app_id');
            if (redirectUri && redirectUri !== 'null' && appId && appId !== 'AURALIFE') {
                const separator = redirectUri.includes('?') ? '&' : '?';
                window.location.href = redirectUri + separator + "token=" + data.accessToken;
            } else {
                window.location.href = "/auth/page/account-chooser";
            }
        } else {
            showToast("Google Login failed", 'error');
        }
    })
    .catch(error => showToast("Connection error.", 'error'));
}

function getDeviceName() {
    let name = "Web Browser";
    if (navigator.userAgent) {
        if (navigator.userAgent.indexOf("Chrome") !== -1) name = "Chrome Browser";
        else if (navigator.userAgent.indexOf("Firefox") !== -1) name = "Firefox Browser";
        else if (navigator.userAgent.indexOf("Safari") !== -1) name = "Safari Browser";
        else if (navigator.userAgent.indexOf("Edge") !== -1) name = "Edge Browser";
    }
    return name;
}

function saveAccount(email, nameStr, token, photoId) {
    if(!email) return;
    let accounts = JSON.parse(localStorage.getItem('savedAccounts')) || [];
    const existingIndex = accounts.findIndex(acc => acc.email === email);
    
    if (existingIndex !== -1) {
        if (nameStr) accounts[existingIndex].name = nameStr;
        if (photoId) accounts[existingIndex].photoId = photoId;
        if (token) {
            accounts[existingIndex].token = token;
            accounts[existingIndex].active = true;
        }
    } else {
        const name = nameStr || email.split('@')[0];
        accounts.push({ 
            name: name, 
            email: email, 
            token: token || null, 
            active: !!token,
            photoId: photoId || null
        });
    }
    localStorage.setItem('savedAccounts', JSON.stringify(accounts));
}

window.signOutAccount = function(email) {
    if(!email) return;
    let accounts = JSON.parse(localStorage.getItem('savedAccounts')) || [];
    const index = accounts.findIndex(acc => acc.email === email);
    if (index !== -1) {
        accounts[index].active = false;
        // DO NOT clear the token here as per user request to "toggle" instead of lose
        localStorage.setItem('savedAccounts', JSON.stringify(accounts));
        
        // If it was the current global token, clear it too
        const currentToken = localStorage.getItem('accessToken');
        if (currentToken) {
            try {
                const payload = JSON.parse(atob(currentToken.split('.')[1]));
                if (payload.sub === email) {
                    localStorage.removeItem('accessToken');
                }
            } catch(e) {}
        }
    }
}

window.resendCode = function() {
    const email = document.getElementById('email').value;
    if(!email) return;
    fetch(`/auth/code/send-again?email=${encodeURIComponent(email)}&type=ACCOUNT_ACTIVISION`, {
        method: 'POST'
    })
    .then(async (res) => {
        const text = await res.text();
        showToast(text, res.ok ? 'success' : 'error');
    })
    .catch(() => showToast('Failed to resend code', 'error'));
}

function showToast(message, type = 'error') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = message;
    container.appendChild(toast);
    setTimeout(() => toast.classList.add('show'), 10);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 4000);
}

