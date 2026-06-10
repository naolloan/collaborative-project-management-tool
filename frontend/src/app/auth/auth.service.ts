import { Injectable } from '@angular/core';
import Keycloak, { KeycloakProfile } from 'keycloak-js';
import { authConfig } from './auth.config';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = new Keycloak(authConfig);
  private initialized = false;

  async initialize(): Promise<boolean> {
    if (this.initialized) {
      return this.isAuthenticated();
    }

    const authenticated = await this.keycloak.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      checkLoginIframe: false
    });

    this.initialized = true;
    return authenticated;
  }

  isAuthenticated(): boolean {
    return Boolean(this.keycloak.authenticated);
  }

  async login(): Promise<void> {
    await this.keycloak.login({
      redirectUri: window.location.origin
    });
  }

  async logout(): Promise<void> {
    await this.keycloak.logout({
      redirectUri: window.location.origin
    });
  }

  async getToken(): Promise<string | undefined> {
    if (!this.isAuthenticated()) {
      return undefined;
    }

    await this.keycloak.updateToken(30);
    return this.keycloak.token;
  }

  async loadProfile(): Promise<KeycloakProfile | undefined> {
    if (!this.isAuthenticated()) {
      return undefined;
    }

    return this.keycloak.loadUserProfile();
  }
}
