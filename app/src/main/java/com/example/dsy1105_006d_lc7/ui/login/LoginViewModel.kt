package com.example.dsy1105_006d_lc7.ui.login

import com.example.dsy1105_006d_lc7.data.model.AuthRepository

/**
 * LoginViewModel que extiende de AuthViewModel para compatibilidad total con el código existente.
 */
class LoginViewModel(repo: AuthRepository = AuthRepository()) : AuthViewModel(repo)