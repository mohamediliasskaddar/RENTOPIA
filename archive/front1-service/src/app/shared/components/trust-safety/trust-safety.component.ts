// src/app/features/trust-safety/trust-safety.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import {RouterLink} from "@angular/router";

interface SafetyFeature {
  icon: string;
  title: string;
  description: string;
  details: string[];
}

interface FAQItem {
  question: string;
  answer: string;
}

@Component({
  selector: 'app-trust-safety',
  standalone: true,
    imports: [
        CommonModule,
        MatIconModule,
        MatCardModule,
        MatButtonModule,
        MatExpansionModule,
        RouterLink
    ],
  templateUrl: './trust-safety.component.html',
  styleUrl: './trust-safety.component.scss'
})
export class TrustSafetyComponent {

  // Features principales
  mainFeatures: SafetyFeature[] = [
    {
      icon: 'shield',
      title: 'Blockchain Security',
      description: 'All transactions recorded on Ethereum for complete transparency',
      details: [
        'Immutable transaction records',
        'Public ledger verification',
        'No central authority control',
        'Cryptographic security'
      ]
    },
    {
      icon: 'lock',
      title: 'Smart Contract Escrow',
      description: 'Automated payment protection for both guests and hosts',
      details: [
        'Funds held securely until check-in',
        'Automatic release after confirmation',
        'No manual intervention needed',
        'Dispute resolution built-in'
      ]
    },
    {
      icon: 'verified',
      title: 'Decentralized Reviews',
      description: 'Authentic reviews stored permanently on-chain',
      details: [
        'Cannot be deleted or modified',
        'Verified blockchain signatures',
        'Complete review history',
        'Transparent rating system'
      ]
    },
    {
      icon: 'gavel',
      title: 'Fair Dispute Resolution',
      description: 'Transparent handling through decentralized protocols',
      details: [
        'Community-driven governance',
        'Evidence stored on blockchain',
        'Fair arbitration process',
        'Binding smart contract decisions'
      ]
    }
  ];

  // Protections pour les guests
  guestProtections: SafetyFeature[] = [
    {
      icon: 'payment',
      title: 'Payment Protection',
      description: 'Your money is safe until you check in',
      details: [
        'Escrow holds payment securely',
        'Release only after check-in confirmation',
        'Full refund for cancellations (within policy)',
        'Dispute protection included'
      ]
    },
    {
      icon: 'verified_user',
      title: 'Verified Properties',
      description: 'Each listing meets our quality standards',
      details: [
        'Property ownership verified on blockchain',
        'Accurate photos and descriptions',
        'Review authenticity guaranteed',
        'Host identity verification'
      ]
    },
    {
      icon: 'support_agent',
      title: '24/7 Support',
      description: 'Help available whenever you need it',
      details: [
        'Live chat support',
        'Emergency assistance',
        'Issue resolution team',
        'Multi-language support'
      ]
    }
  ];

  // Protections pour les hosts
  hostProtections: SafetyFeature[] = [
    {
      icon: 'account_balance',
      title: 'Guaranteed Payments',
      description: 'Get paid securely via smart contracts',
      details: [
        'Automatic payment release',
        'No chargebacks',
        'Cryptocurrency stability',
        'Instant settlement'
      ]
    },
    {
      icon: 'security',
      title: 'Property Protection',
      description: 'Coverage for damages and issues',
      details: [
        'Security deposit held in escrow',
        'Damage claim process',
        'Evidence submitted on-chain',
        'Fair resolution protocol'
      ]
    },
    {
      icon: 'privacy_tip',
      title: 'Privacy Control',
      description: 'You control your information',
      details: [
        'Selective information sharing',
        'Guest screening tools',
        'Communication on your terms',
        'Data ownership rights'
      ]
    }
  ];

  // FAQ
  faqs: FAQItem[] = [
    {
      question: 'How does blockchain make rentals safer?',
      answer: 'Blockchain provides an immutable, transparent record of all transactions. Smart contracts automate escrow, ensuring payments are only released when conditions are met. All reviews are permanently stored and cannot be manipulated.'
    },
    {
      question: 'What happens if there\'s a dispute?',
      answer: 'Our smart contract includes a built-in dispute resolution mechanism. Both parties can submit evidence to the blockchain, and the case is reviewed through our decentralized governance protocol for fair arbitration.'
    },
    {
      question: 'Is my payment information secure?',
      answer: 'Yes. We use Ethereum blockchain for all transactions, which means your payment details are secured by cryptographic encryption. We never store your private keys or sensitive financial information.'
    },
    {
      question: 'Can hosts cancel my reservation?',
      answer: 'Host cancellations are heavily discouraged. If a host cancels, you receive an immediate full refund through the smart contract, and the host faces penalties including reduced visibility and potential account restrictions.'
    },
    {
      question: 'What if the property doesn\'t match the listing?',
      answer: 'Contact our support team immediately upon arrival. We can initiate a dispute resolution process and, if the property is significantly different, arrange a full refund and help you find alternative accommodation.'
    },
    {
      question: 'How are guest damages handled?',
      answer: 'Security deposits are held in smart contract escrow. If damages occur, the host submits evidence to the blockchain. The claim is reviewed, and if valid, the appropriate amount is released from the deposit.'
    }
  ];
}
