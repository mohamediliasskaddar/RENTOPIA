// src/app/features/about/about.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

interface TeamMember {
  name: string;
  role: string;
  image: string;
  bio: string;
}

interface Milestone {
  year: string;
  title: string;
  description: string;
}

interface Value {
  icon: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    RouterLink
  ],
  templateUrl: './about.component.html',
  styleUrl: './about.component.scss'
})
export class AboutComponent {

  // Company values
  values: Value[] = [
    {
      icon: 'verified',
      title: 'Transparency',
      description: 'Every transaction recorded on blockchain for complete visibility and trust'
    },
    {
      icon: 'security',
      title: 'Security',
      description: 'Cryptographic protection and smart contracts ensure your safety'
    },
    {
      icon: 'groups',
      title: 'Decentralization',
      description: 'No single authority controls the platform - power to the community'
    },
    {
      icon: 'auto_awesome',
      title: 'Innovation',
      description: 'Pioneering Web3 technology to revolutionize real estate rentals'
    }
  ];

  // Company milestones
  milestones: Milestone[] = [
    {
      year: '2022',
      title: 'Foundation',
      description: 'RentalChain was founded with a vision to decentralize real estate rentals using blockchain technology'
    },
    {
      year: '2023',
      title: 'Smart Contracts Launch',
      description: 'Deployed our first smart contracts on Ethereum mainnet, enabling trustless escrow and payments'
    },
    {
      year: '2024',
      title: 'Platform Launch',
      description: 'Officially launched the platform with 1,000+ verified properties across Morocco'
    },
    {
      year: '2025',
      title: 'Global Expansion',
      description: 'Expanding to international markets with 10,000+ properties and growing community'
    }
  ];

  // Team members (you can add real team or keep generic)
  teamMembers: TeamMember[] = [
    {
      name: 'Sarah Johnson',
      role: 'CEO & Co-Founder',
      image: 'assets/team/placeholder.jpg',
      bio: 'Blockchain expert with 10+ years in real estate technology'
    },
    {
      name: 'Michael Chen',
      role: 'CTO & Co-Founder',
      image: 'assets/team/placeholder.jpg',
      bio: 'Smart contract developer and Ethereum core contributor'
    },
    {
      name: 'Emma Davis',
      role: 'Head of Product',
      image: 'assets/team/placeholder.jpg',
      bio: 'Product designer focused on Web3 user experience'
    },
    {
      name: 'James Wilson',
      role: 'Head of Community',
      image: 'assets/team/placeholder.jpg',
      bio: 'Building trust and engagement in our decentralized community'
    }
  ];

  // Stats
  stats = [
    { value: '10,000+', label: 'Properties Listed' },
    { value: '50,000+', label: 'Active Users' },
    { value: '100+', label: 'Cities Worldwide' },
    { value: '$2M+', label: 'Total Volume' }
  ];

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).join('');
  }
}
