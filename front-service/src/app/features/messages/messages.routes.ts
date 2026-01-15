import { Routes } from '@angular/router';

export const MESSAGES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./conversations-list/conversation-list.component')
      .then(m => m.ConversationListComponent),
    data: { title: 'My Messages' }
  },
  {
    path: ':id',
    loadComponent: () => import('./chat-view/chat-view.component')
      .then(m => m.ChatViewComponent),
    data: { title: 'Chat' }
  }
];
