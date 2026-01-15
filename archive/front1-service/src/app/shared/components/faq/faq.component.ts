// src/app/features/faq/faq.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import {RouterLink} from "@angular/router";

interface FAQItem {
  id: number;
  category: string;
  question: string;
  answer: string;
}

interface Category {
  name: string;
  icon: string;
  count: number;
}

@Component({
  selector: 'app-faq',
  standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatIconModule,
        MatExpansionModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatChipsModule,
        RouterLink
    ],
  templateUrl: './faq.component.html',
  styleUrl: './faq.component.scss'
})
export class FaqComponent {

  searchQuery: string = '';
  selectedCategory: string = 'all';

  categories: Category[] = [
    { name: 'all', icon: 'help', count: 0 },
    { name: 'booking', icon: 'event', count: 0 },
    { name: 'payments', icon: 'payment', count: 0 },
    { name: 'hosting', icon: 'home_work', count: 0 },
    { name: 'blockchain', icon: 'link', count: 0 },
    { name: 'account', icon: 'person', count: 0 },
    { name: 'safety', icon: 'shield', count: 0 }
  ];

  faqs: FAQItem[] = [
    // BOOKING
    {
      id: 1,
      category: 'booking',
      question: 'How do I book a property?',
      answer: 'Search for your destination, select dates, choose a property, and click "Book Now". You can book instantly or send a request to the host. Payment is made securely via Ethereum.'
    },
    {
      id: 2,
      category: 'booking',
      question: 'Can I modify my reservation?',
      answer: 'Yes, you can modify dates through your account dashboard. The host must approve changes. If approved, any price difference will be handled through the smart contract automatically.'
    },
    {
      id: 3,
      category: 'booking',
      question: 'What is instant booking?',
      answer: 'Instant booking allows you to reserve a property immediately without waiting for host approval. The smart contract is executed instantly and payment is held in escrow.'
    },
    {
      id: 4,
      category: 'booking',
      question: 'How far in advance can I book?',
      answer: 'You can book up to 12 months in advance, depending on the host\'s availability calendar. Some hosts may have shorter booking windows.'
    },

    // PAYMENTS
    {
      id: 5,
      category: 'payments',
      question: 'What payment methods do you accept?',
      answer: 'We accept Ethereum (ETH) cryptocurrency. You need a Web3 wallet like MetaMask to make payments. All transactions are processed on the Ethereum blockchain.'
    },
    {
      id: 6,
      category: 'payments',
      question: 'When is payment charged?',
      answer: 'Payment is charged immediately upon booking and held in a smart contract escrow. It\'s released to the host 24 hours after your check-in, ensuring both parties are protected.'
    },
    {
      id: 7,
      category: 'payments',
      question: 'Are there any platform fees?',
      answer: 'We charge 0% platform fees! You only pay blockchain gas fees for transactions. This is one of the benefits of our decentralized model - no middleman taking a cut.'
    },
    {
      id: 8,
      category: 'payments',
      question: 'Is my payment information secure?',
      answer: 'Absolutely. All payments are processed through Ethereum blockchain using cryptographic security. We never store your wallet private keys or sensitive financial information.'
    },
    {
      id: 9,
      category: 'payments',
      question: 'What are gas fees?',
      answer: 'Gas fees are network transaction costs on Ethereum. They vary based on network congestion and go to blockchain validators, not to us. We recommend transacting during low-traffic hours to minimize fees.'
    },

    // HOSTING
    {
      id: 10,
      category: 'hosting',
      question: 'How do I become a host?',
      answer: 'Click "Become a Host", create your listing with photos and details, set your pricing and availability, then submit for review. Once approved, your property goes live on the blockchain.'
    },
    {
      id: 11,
      category: 'hosting',
      question: 'How much does it cost to list my property?',
      answer: 'Listing is completely free! We charge 0% platform fees. You only pay minimal gas fees when creating your listing smart contract on the blockchain.'
    },
    {
      id: 12,
      category: 'hosting',
      question: 'When do I get paid?',
      answer: 'Payment is automatically released from escrow 24 hours after guest check-in. The smart contract transfers ETH directly to your wallet - no waiting for manual processing.'
    },
    {
      id: 13,
      category: 'hosting',
      question: 'Can I set different prices for weekends?',
      answer: 'Yes! You can set custom pricing for weekends, holidays, and special events. The smart contract automatically applies the correct rate based on booking dates.'
    },
    {
      id: 14,
      category: 'hosting',
      question: 'What if a guest damages my property?',
      answer: 'Security deposits are held in smart contract escrow. Submit damage evidence to the blockchain, and if validated through our dispute process, funds are released from the deposit.'
    },

    // BLOCKCHAIN
    {
      id: 15,
      category: 'blockchain',
      question: 'What is blockchain and why do you use it?',
      answer: 'Blockchain is a decentralized, transparent ledger. We use it for immutable transaction records, automated smart contracts, and trustless escrow - eliminating the need for intermediaries.'
    },
    {
      id: 16,
      category: 'blockchain',
      question: 'Do I need to understand blockchain to use the platform?',
      answer: 'No! While everything runs on blockchain, our interface is user-friendly. You just need a MetaMask wallet (we provide setup guides) and some ETH for bookings.'
    },
    {
      id: 17,
      category: 'blockchain',
      question: 'What is a smart contract?',
      answer: 'A smart contract is self-executing code on the blockchain. It automatically handles escrow, payments, and refunds based on predefined conditions - no manual intervention needed.'
    },
    {
      id: 18,
      category: 'blockchain',
      question: 'Can transactions be reversed?',
      answer: 'Blockchain transactions are irreversible once confirmed. However, our smart contracts include built-in refund mechanisms for cancellations and disputes within policy terms.'
    },

    // ACCOUNT
    {
      id: 19,
      category: 'account',
      question: 'How do I create an account?',
      answer: 'Click "Sign Up", connect your Web3 wallet (like MetaMask), verify your email, and complete your profile. Your wallet address serves as your unique account identifier.'
    },
    {
      id: 20,
      category: 'account',
      question: 'I forgot my wallet password, what do I do?',
      answer: 'Wallet recovery depends on your wallet provider (e.g., MetaMask). Use your seed phrase to restore access. We cannot recover wallets as we don\'t store your private keys.'
    },
    {
      id: 21,
      category: 'account',
      question: 'Can I change my wallet address?',
      answer: 'Your wallet address is your identity on blockchain. To use a different wallet, create a new account. You cannot transfer existing bookings/reviews to a new wallet.'
    },
    {
      id: 22,
      category: 'account',
      question: 'How do I verify my account?',
      answer: 'Complete email verification, phone verification (optional), and identity verification for hosts. Verified accounts have higher trust scores and better visibility.'
    },

    // SAFETY
    {
      id: 23,
      category: 'safety',
      question: 'How do you verify properties?',
      answer: 'Property ownership is verified on blockchain. We check listing accuracy, review photos, and validate host identity. Community reviews provide additional authenticity verification.'
    },
    {
      id: 24,
      category: 'safety',
      question: 'What if the property doesn\'t match the listing?',
      answer: 'Contact support immediately upon arrival. We can initiate dispute resolution, and if the property is significantly different, you\'ll receive a full refund via smart contract.'
    },
    {
      id: 25,
      category: 'safety',
      question: 'Are reviews authentic?',
      answer: 'Yes! All reviews are stored permanently on blockchain and cannot be deleted or modified. Only verified guests who completed stays can leave reviews.'
    },
    {
      id: 26,
      category: 'safety',
      question: 'What happens if there\'s a dispute?',
      answer: 'Submit evidence to the blockchain. Our decentralized governance protocol reviews the case with community arbitrators, ensuring fair and transparent resolution.'
    },
    {
      id: 27,
      category: 'safety',
      question: 'Is my personal data secure?',
      answer: 'We use encryption for sensitive data. Blockchain stores only transaction and review data - personal information is kept private and under your control.'
    },

    // CANCELLATIONS
    {
      id: 28,
      category: 'booking',
      question: 'What is the cancellation policy?',
      answer: 'Each property has its own policy (Flexible, Moderate, or Strict). The smart contract automatically calculates refunds based on the policy and cancellation timing.'
    },
    {
      id: 29,
      category: 'booking',
      question: 'Can the host cancel my reservation?',
      answer: 'Hosts can cancel, but face penalties: reduced visibility, negative impact on ratings, and potential account restrictions. You receive immediate full refund via smart contract.'
    },
    {
      id: 30,
      category: 'payments',
      question: 'How long do refunds take?',
      answer: 'Smart contract refunds are instant! Once cancellation is processed, ETH is automatically returned to your wallet within minutes - no waiting for bank processing.'
    }
  ];

  constructor() {
    this.updateCategoryCounts();
  }

  private updateCategoryCounts(): void {
    this.categories.forEach(cat => {
      if (cat.name === 'all') {
        cat.count = this.faqs.length;
      } else {
        cat.count = this.faqs.filter(faq => faq.category === cat.name).length;
      }
    });
  }

  get filteredFAQs(): FAQItem[] {
    let filtered = this.faqs;

    // Filter by category
    if (this.selectedCategory !== 'all') {
      filtered = filtered.filter(faq => faq.category === this.selectedCategory);
    }

    // Filter by search query
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(faq =>
        faq.question.toLowerCase().includes(query) ||
        faq.answer.toLowerCase().includes(query)
      );
    }

    return filtered;
  }

  selectCategory(category: string): void {
    this.selectedCategory = category;
  }

  clearSearch(): void {
    this.searchQuery = '';
  }

  getCategoryLabel(category: string): string {
    return category.charAt(0).toUpperCase() + category.slice(1);
  }
}
