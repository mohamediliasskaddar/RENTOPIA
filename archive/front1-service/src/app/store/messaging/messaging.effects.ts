// src/app/store/messaging/messaging.effects.ts

import {inject, Injectable} from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, mergeMap, catchError, tap, switchMap } from 'rxjs/operators';
import { MessagingService } from '../../core/services/messaging.service';
import { WebSocketService, WebSocketMessageType } from '../../core/services/websocket.service';
import * as MessagingActions from './messaging.actions';

@Injectable()
export class MessagingEffects {


  private actions$ = inject(Actions);
  private messagingService = inject(MessagingService);
  private wsService=inject(WebSocketService)

  // ==================== CONVERSATIONS ====================

  loadConversations$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MessagingActions.loadConversations),
      mergeMap(() =>
        this.messagingService.getMyConversations().pipe(
          map(conversations => MessagingActions.loadConversationsSuccess({ conversations })),
          catchError(error => of(MessagingActions.loadConversationsFailure({
            error: error.message
          })))
        )
      )
    )
  );

  loadConversationDetails$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MessagingActions.loadConversationDetails),
      mergeMap(({ conversationId }) =>
        this.messagingService.getConversationById(conversationId).pipe(
          map(conversation => MessagingActions.loadConversationDetailsSuccess({ conversation })),
          catchError(error => of(MessagingActions.loadConversationsFailure({
            error: error.message
          })))
        )
      )
    )
  );

  createConversation$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MessagingActions.createConversation),
      mergeMap(({ request }) =>
        this.messagingService.createConversation(request).pipe(
          map(conversation => MessagingActions.createConversationSuccess({ conversation })),
          catchError(error => of(MessagingActions.loadConversationsFailure({
            error: error.message
          })))
        )
      )
    )
  );

  archiveConversation$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MessagingActions.archiveConversation),
      mergeMap(({ conversationId }) =>
        this.messagingService.archiveConversation(conversationId).pipe(
          map(() => MessagingActions.loadConversations()),
          catchError(error => of(MessagingActions.loadConversationsFailure({
            error: error.message
          })))
        )
      )
    )
  );

  // ==================== MESSAGES ====================

  loadMessages$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MessagingActions.loadMessages),
      mergeMap(({ conversationId }) =>
        this.messagingService.getConversationMessages(conversationId).pipe(
          map(messages => MessagingActions.loadMessagesSuccess({ conversationId, messages })),
          catchError(error => of(MessagingActions.sendMessageFailure({
            error: error.message
          })))
        )
      )
    )
  );

  sendMessage$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MessagingActions.sendMessage),
      mergeMap(({ request }) =>
        this.messagingService.sendMessage(request).pipe(
          map(message => MessagingActions.sendMessageSuccess({ message })),
          catchError(error => of(MessagingActions.sendMessageFailure({
            error: error.message
          })))
        )
      )
    )
  );

  markAsRead$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MessagingActions.markMessagesAsRead),
      mergeMap(({ conversationId }) =>
        this.messagingService.markMessagesAsRead(conversationId).pipe(
          map(() => MessagingActions.markMessagesAsReadSuccess({ conversationId })),
          catchError(error => of(MessagingActions.sendMessageFailure({
            error: error.message
          })))
        )
      )
    )
  );

  // ==================== WEBSOCKET ====================

  connectWebSocket$ = createEffect(() =>
      this.actions$.pipe(
        ofType(MessagingActions.connectWebSocket),
        tap(({ token }) => {
          this.wsService.connect(token);
        })
      ),
    { dispatch: false }
  );

  disconnectWebSocket$ = createEffect(() =>
      this.actions$.pipe(
        ofType(MessagingActions.disconnectWebSocket),
        tap(() => {
          this.wsService.disconnect();
        })
      ),
    { dispatch: false }
  );

  listenToWebSocket$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MessagingActions.connectWebSocket),
      switchMap(() =>
        this.wsService.messages$.pipe(
          switchMap(wsMessage => {
            if (wsMessage.type === WebSocketMessageType.NEW_MESSAGE) {
              return of(
                MessagingActions.receiveWebSocketMessage({
                  message: wsMessage.data
                })
              );
            }
            return of();
          })
        )
      )
    )
  );


  webSocketConnected$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MessagingActions.connectWebSocket),
      switchMap(() =>
        this.wsService.connected$.pipe(
          map(connected =>
            connected
              ? MessagingActions.webSocketConnected()
              : MessagingActions.webSocketDisconnected()
          )
        )
      )
    )
  );


  joinConversation$ = createEffect(() =>
      this.actions$.pipe(
        ofType(MessagingActions.joinConversation),
        tap(({ conversationId }) => {
          this.wsService.joinConversation(conversationId);
        })
      ),
    { dispatch: false }
  );

  // ✅ NOUVEAU: Effect pour charger par réservation
  loadConversationByReservation$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MessagingActions.loadConversationByReservation),
      mergeMap(({ reservationId }) =>
        this.messagingService.getConversationByReservationId(reservationId).pipe(
          map(conversation =>
            MessagingActions.loadConversationByReservationSuccess({ conversation })
          ),
          catchError(error =>
            of(MessagingActions.loadConversationByReservationFailure({
              error: error.message
            }))
          )
        )
      )
    )
  );
}
