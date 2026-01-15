// src/app/core/services/web3.service.ts

import { Injectable } from '@angular/core';
import { BrowserProvider, JsonRpcSigner } from 'ethers';
import { Observable, from, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { parseEther, formatEther } from "ethers";

declare global {
  interface Window {
    ethereum?: any;
  }
}

@Injectable({
  providedIn: 'root'
})
export class Web3Service {
  private provider: BrowserProvider | null = null;
  private signer: JsonRpcSigner | null = null;

  constructor() {}

  isMetaMaskInstalled(): boolean {
    return typeof window.ethereum !== 'undefined';
  }

  connectWallet(): Observable<string> {
    if (!this.isMetaMaskInstalled()) {
      return throwError(() => new Error(
        'MetaMask n\'est pas installé. Veuillez l\'installer depuis metamask.io'
      ));
    }

    return from(this.connectMetaMask()).pipe(
      catchError(error => {
        console.error('❌ Erreur connexion MetaMask:', error);

        if (error.code === 4001) {
          return throwError(() => new Error('Connexion refusée par l\'utilisateur'));
        }

        return throwError(() => new Error('Erreur lors de la connexion à MetaMask'));
      })
    );
  }

  private async connectMetaMask(): Promise<string> {
    try {
      this.provider = new BrowserProvider(window.ethereum);
      await window.ethereum.request({ method: 'eth_requestAccounts' });
      this.signer = await this.provider.getSigner();
      const address = await this.signer.getAddress();
      return address;
    } catch (error) {
      throw error;
    }
  }

  signMessage(message: string): Observable<string> {
    if (!this.signer) {
      return throwError(() => new Error('Wallet non connecté. Connectez-vous d\'abord.'));
    }

    return from(this.signer.signMessage(message)).pipe(
      catchError(error => {
        console.error('❌ Erreur signature:', error);

        if (error.code === 4001) {
          return throwError(() => new Error('Signature refusée par l\'utilisateur'));
        }

        return throwError(() => new Error('Erreur lors de la signature du message'));
      })
    );
  }

  getCurrentAccount(): Observable<string | null> {
    if (!this.isMetaMaskInstalled()) {
      return throwError(() => new Error('MetaMask non installé'));
    }

    return from(window.ethereum.request({ method: 'eth_accounts' }) as Promise<string[]>).pipe(
      map((accounts: string[]) => accounts.length > 0 ? accounts[0] : null),
      catchError(() => throwError(() => new Error('Impossible de récupérer le compte')))
    );
  }

  disconnectWallet(): void {
    this.provider = null;
    this.signer = null;
  }

  onAccountsChanged(callback: (accounts: string[]) => void): void {
    if (this.isMetaMaskInstalled()) {
      window.ethereum.on('accountsChanged', callback);
    }
  }

  onChainChanged(callback: (chainId: string) => void): void {
    if (this.isMetaMaskInstalled()) {
      window.ethereum.on('chainChanged', callback);
    }
  }

  generateAuthMessage(walletAddress: string): string {
    const timestamp = Date.now();
    return `Sign this message to authenticate with Real Estate Rent DApp.

Wallet: ${walletAddress}
Timestamp: ${timestamp}`;
  }

  async checkNetwork(): Promise<boolean> {
    try {
      const chainId = await window.ethereum.request({ method: 'eth_chainId' });
      return chainId === environment.blockchain.chainId;
    } catch (error) {
      console.error('❌ Erreur vérification réseau:', error);
      return false;
    }
  }

  async switchNetwork(): Promise<void> {
    try {
      await window.ethereum.request({
        method: 'wallet_switchEthereumChain',
        params: [{ chainId: environment.blockchain.chainId }]
      });
    } catch (error: any) {
      if (error.code === 4902) {
        await this.addNetwork();
      } else {
        throw error;
      }
    }
  }

  private async addNetwork(): Promise<void> {
    await window.ethereum.request({
      method: 'wallet_addEthereumChain',
      params: [{
        chainId: environment.blockchain.chainId,
        chainName: environment.blockchain.chainName,
        rpcUrls: [environment.blockchain.rpcUrl]
      }]
    });
  }

  /**
   * ✅ CORRECTION CRITIQUE : Convertir ETH en Wei
   * IMPORTANT : Vous DEVEZ passer le montant en ETH (ex: 0.044)
   * Cette fonction va le convertir en Wei (0.044 ETH = 44000000000000000 Wei)
   *
   * @param eth - Montant en ETH (nombre décimal comme 0.044)
   * @returns String en Wei (format hexadécimal pour MetaMask)
   */
  ethToWei(eth: number): string {
    console.log('🔄 Conversion ETH → Wei:', eth, 'ETH');

    // ✅ CORRECTION : Arrondir à 6 décimales pour éviter les erreurs de précision
    const ethRounded = Number(eth.toFixed(6));

    // Convertir en Wei (1 ETH = 10^18 Wei)
    const weiValue = parseEther(ethRounded.toString());

    // Convertir en format hexadécimal pour MetaMask
    const hexValue = '0x' + weiValue.toString(16);

    console.log('✅ Résultat:', {
      ethInput: eth,
      ethRounded: ethRounded,
      weiBigInt: weiValue.toString(),
      hexValue: hexValue
    });

    return hexValue;
  }

  /**
   * ✅ Convertir Wei en ETH
   * @param wei - Montant en Wei (string ou nombre)
   * @returns Nombre en ETH
   */
  weiToEth(wei: string | bigint): number {
    return parseFloat(formatEther(wei));
  }

  /**
   * ✅ NOUVEAU : Vérifier le solde du wallet
   * @param walletAddress - Adresse du wallet
   * @returns Observable<number> - Solde en ETH
   */
  getBalance(walletAddress: string): Observable<number> {
    if (!this.provider) {
      return throwError(() => new Error('Provider non initialisé'));
    }

    return from(this.provider.getBalance(walletAddress)).pipe(
      map(balance => this.weiToEth(balance)),
      catchError(error => {
        console.error('❌ Erreur récupération solde:', error);
        return throwError(() => new Error('Impossible de récupérer le solde'));
      })
    );
  }
}
