import { Component, inject } from '@angular/core';
import { AuthService } from '../../access/auth.service';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.html',
})
export class HomePage {
  protected readonly user = inject(AuthService).user;
}
