import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { OrganizationalUnit, OrganizationalUnitType } from '../../core/dto/organizational-unit';

@Component({
  selector: 'app-units-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './units-page.component.html'
})
export class UnitsPageComponent {
  @Input({ required: true }) organizationalUnits: OrganizationalUnit[] = [];
  @Input({ required: true }) organizationalUnitTypes: OrganizationalUnitType[] = [];
  @Input({ required: true }) unitStatus = 'Not loaded';
  @Input({ required: true }) canManage = false;
  @Input({ required: true }) creatingUnit = false;
  @Input({ required: true }) updatingUnit = false;
  @Input({ required: true }) deactivatingUnit = false;
  @Input({ required: true }) newUnit!: {
    name: string;
    type: OrganizationalUnitType;
    description: string;
  };
  @Input({ required: true }) editUnit!: {
    id: number | null;
    name: string;
    type: OrganizationalUnitType;
    description: string;
  };

  @Output() refreshUnits = new EventEmitter<void>();
  @Output() createUnit = new EventEmitter<void>();
  @Output() selectUnit = new EventEmitter<OrganizationalUnit>();
  @Output() updateUnit = new EventEmitter<void>();
  @Output() deactivateUnit = new EventEmitter<void>();

  formatUnitType(type: string | null | undefined): string {
    if (!type) {
      return 'Unassigned';
    }

    return type.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  }
}
