import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-analysis',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analysis.html',
  styleUrl: './analysis.css',
})
export class Analysis {

  constructor(public router: Router) { }

  goBack() {
    this.router.navigate(['/dashboard']);
  }
}
