import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MatToolbarModule, MatIconModule],
  template: `
    <div class="app-shell">
      <nav class="topbar">
        <div class="topbar-inner">
          <div class="brand">
            <div class="logo-ring">
              <mat-icon>cloud_upload</mat-icon>
            </div>
            <div class="brand-text">
              <span class="brand-name">FileVault</span>
              <span class="brand-tag">Serverless Resume Manager</span>
            </div>
          </div>
          <div class="badge">
            <span class="dot"></span>
            Azure Functions
          </div>
        </div>
      </nav>
      <main class="content animate-in">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .app-shell {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    .topbar {
      position: sticky;
      top: 0;
      z-index: 100;
      background: rgba(2, 6, 23, 0.82);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      border-bottom: 1px solid var(--border);
    }

    .topbar-inner {
      max-width: 1280px;
      margin: 0 auto;
      padding: 14px 28px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .logo-ring {
      width: 42px;
      height: 42px;
      border-radius: 12px;
      background: linear-gradient(135deg, var(--accent), var(--accent-light));
      display: grid;
      place-items: center;
      box-shadow: 0 0 20px rgba(0,120,212,0.35);
      animation: pulse-glow 3s ease-in-out infinite;

      mat-icon {
        color: #fff;
        font-size: 22px;
        width: 22px;
        height: 22px;
      }
    }

    .brand-name {
      font-weight: 700;
      font-size: 20px;
      letter-spacing: -0.02em;
      background: linear-gradient(135deg, #fff 30%, var(--accent-light));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }

    .brand-tag {
      display: block;
      font-size: 11px;
      color: var(--text-muted);
      letter-spacing: 0.04em;
      margin-top: 1px;
    }

    .badge {
      display: flex;
      align-items: center;
      gap: 7px;
      font-size: 12px;
      font-weight: 500;
      color: var(--accent-light);
      background: rgba(80,230,255,0.08);
      border: 1px solid rgba(80,230,255,0.18);
      padding: 6px 14px;
      border-radius: 20px;

      .dot {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        background: var(--success);
        box-shadow: 0 0 6px var(--success);
        animation: pulse-glow 2s ease-in-out infinite;
      }
    }

    .content {
      flex: 1;
      padding: 32px 28px;
      max-width: 1280px;
      width: 100%;
      margin: 0 auto;
    }

    @media (max-width: 640px) {
      .topbar-inner { padding: 12px 16px; }
      .content { padding: 20px 16px; }
      .badge { display: none; }
    }
  `]
})
export class AppComponent {
  title = 'FileVault';
}
