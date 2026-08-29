# Master Prompt: Offline Encrypted Peer-to-Peer Messaging App

Build a complete mobile application called **[APP NAME]** that allows two or more phones running the same app to communicate through text messages **without requiring Wi-Fi internet access, mobile data, or a cellular network**.

The app must prioritize:

1. Offline communication
2. End-to-end encryption
3. Privacy
4. Reliable peer-to-peer communication
5. Simple, modern UI
6. No cloud server dependency for basic messaging

## 1. Core Concept

The application is an **offline peer-to-peer encrypted messenger**.

Two phones with the application installed should be able to discover each other and establish a direct local connection using available device-to-device communication technologies.

Preferred communication technologies:

* Android: Wi-Fi Direct / Nearby Connections where appropriate
* Android: Bluetooth Low Energy (BLE) for discovery and fallback communication
* iOS: Multipeer Connectivity where applicable
* If developing cross-platform, create platform-specific networking layers behind a common messaging interface.

The application must NOT require:

* Internet
* Mobile data
* SIM card
* Cloud server
* Firebase
* WhatsApp
* Telegram
* Email
* External messaging APIs

The application may use Bluetooth or Wi-Fi Direct internally, but this must not require an internet connection.

## 2. Important Networking Requirement

Do NOT assume that "Wi-Fi off" means the app cannot communicate.

The goal is:

Phone A
↓
Local peer-to-peer connection
↓
Phone B

No internet router or cloud server should be required.

Implement a transport abstraction:

```text
Transport Layer
    ├── Wi-Fi Direct / local peer-to-peer
    ├── BLE
    └── Other platform-supported offline transport
```

The messaging layer should not depend directly on one transport.

Create an interface similar to:

```text
PeerTransport

discoverPeers()
connectToPeer()
disconnectFromPeer()
sendData()
receiveData()
getConnectionState()
```

This allows the transport implementation to change without rewriting the chat system.

## 3. Device Discovery

When the user opens the application:

Display:

"Searching for nearby devices..."

The application should discover nearby devices running the same application.

Each device should have:

* Randomly generated device ID
* Display name
* Public key
* Connection status

Do NOT expose the user's phone number by default.

Example:

```text
Nearby Devices

● Alex's Phone
  Available

● Device-8F31
  Available
```

Allow the user to change their display name.

Do not use personally identifying device information as the public identity.

## 4. Pairing

Before allowing messaging, establish a secure authenticated connection.

When another device is selected:

```text
Connect to Alex's Phone?

[Cancel] [Connect]
```

Both devices should receive a connection request.

For stronger authentication, provide optional verification using:

* QR code
* Safety number
* Short authentication code

Example:

```text
Security Verification

Compare this code on both phones:

482 193

If both codes match:

[Verified]
```

The application should warn users if the peer's identity key changes unexpectedly.

## 5. Encryption

All messages MUST be encrypted before transmission.

Do NOT implement custom cryptographic algorithms.

Use established, audited cryptographic primitives/libraries.

Preferred architecture:

### Identity

Each installation generates a long-term public/private key pair.

The private key:

* Never leaves the device
* Is stored using secure platform storage
* Is never transmitted to another device

The public key may be exchanged with peers.

### Session encryption

After establishing a connection, perform authenticated key agreement.

Use a modern protocol such as:

* X25519 for key agreement
* Ed25519 or an appropriate modern signature scheme for identity/authentication
* HKDF for key derivation
* ChaCha20-Poly1305 or AES-256-GCM for authenticated encryption

Prefer an established protocol/library such as the Signal Protocol or another well-reviewed secure messaging protocol rather than designing a new cryptographic protocol.

Each message should use authenticated encryption.

Conceptually:

```text
Plaintext Message
        ↓
Session Encryption
        ↓
Ciphertext + Authentication Data
        ↓
Offline Transport
        ↓
Receiver
        ↓
Decryption
        ↓
Plaintext Message
```

Never transmit plaintext messages over the transport layer.

## 6. Perfect Forward Secrecy

If practical for the selected cryptographic library/protocol, implement forward secrecy.

Compromise of a long-term identity key should NOT automatically reveal previously exchanged messages.

Use an established ratcheting protocol such as the Double Ratchet when supported by the chosen messaging library.

Do not invent your own ratchet implementation unless absolutely necessary.

## 7. Chat Interface

Create a clean modern chat interface.

Example:

```text
← Alex                         🔒

        Today

        Hey!
                         Hi! 👋

        Are you nearby?
                         Yes

Type a message...       ➤
```

Features:

* Message bubbles
* Timestamps
* Sent indicator
* Delivered indicator
* Read indicator where technically possible
* Message input box
* Send button
* Connection status
* Encryption indicator

Display:

```text
🔒 End-to-end encrypted
```

Do not display encryption keys in the normal chat interface.

## 8. Message States

Every message should have a state:

```text
PENDING
SENDING
SENT
DELIVERED
READ
FAILED
```

Example:

```text
Hello!
        ✓ Sent

How are you?
        ✓✓ Delivered

Are you coming?
        ✓✓ Read
```

If the connection disappears:

```text
Message pending

Waiting for connection...
```

When the peer reconnects, attempt delivery.

## 9. Offline Message Queue

Messages should be stored locally when the recipient is temporarily unavailable.

Example:

```text
You
    "I'll meet you at 5"

Status:
    Waiting for Alex to reconnect
```

Once the peer reconnects:

```text
Sending...
Delivered ✓
```

Encrypted messages stored locally should remain encrypted at rest.

## 10. Local Database

Use a local database such as:

* SQLite
* Room on Android
* Core Data / SwiftData on iOS
* Appropriate cross-platform local database

Store:

```text
Conversation
    id
    peerId
    createdAt
    lastMessageAt

Message
    id
    conversationId
    senderId
    encryptedPayload
    timestamp
    status
```

Do not store plaintext message content unnecessarily.

## 11. Key Storage

Private keys must be stored securely.

Android:

Use Android Keystore where appropriate.

iOS:

Use Keychain/Secure Enclave capabilities where appropriate.

Never:

* Put private keys in SharedPreferences
* Put private keys in plaintext files
* Put private keys in the database unprotected
* Log private keys
* Send private keys over the network

## 12. Connection Management

The application must gracefully handle:

* Peer appears
* Peer disappears
* Connection established
* Connection lost
* Reconnection
* Device goes to background
* Device returns to foreground
* Bluetooth disabled
* Wi-Fi disabled
* Permissions denied
* Peer rejects connection

Display understandable errors.

Example:

```text
Connection lost

Alex's Phone is no longer reachable.

Messages will remain queued until
the connection is restored.
```

## 13. Multi-Device / Group Messaging

Design the architecture so group messaging can be added later.

For version 1, prioritize:

**One-to-one messaging.**

After that, optionally support:

```text
Group Chat
    ↓
Phone A
Phone B
Phone C
Phone D
```

Do NOT send a single plaintext message to every peer.

Use appropriate group encryption.

## 14. No Central Server

The initial version must function without a central server.

Do not require:

```text
App
 ↓
Internet
 ↓
Server
 ↓
Recipient
```

Instead:

```text
Phone A
    ↕
Local encrypted connection
    ↕
Phone B
```

The serverless architecture should be a core requirement.

## 15. Privacy

The application should collect as little information as possible.

Do not require account creation.

Do not require:

* Email
* Phone number
* Social media account
* Cloud account

The user's identity should primarily be represented by their local cryptographic identity.

Provide an option:

```text
Regenerate Identity
```

with a warning that this will affect existing trusted-peer relationships.

## 16. Security Requirements

Never:

* Implement homemade encryption
* Hard-code encryption keys
* Store plaintext passwords
* Log plaintext messages
* Log private keys
* Send private keys
* Trust peers without authentication
* Disable TLS/security checks merely to make development work
* Use obsolete cryptographic algorithms

Use established cryptographic libraries.

Add protections against:

* Replay attacks
* Message tampering
* Impersonation
* Duplicate messages
* Message reordering
* Malformed packets
* Unauthorized peer connections

Every message should have a unique identifier.

Example:

```text
messageId
senderId
timestamp
sequenceNumber
ciphertext
authenticationTag
```

## 17. Protocol Structure

Define a versioned message protocol.

Example:

```json
{
  "version": 1,
  "type": "MESSAGE",
  "messageId": "...",
  "senderId": "...",
  "conversationId": "...",
  "timestamp": 0,
  "payload": "ENCRYPTED_DATA"
}
```

Handshake messages should be separate from application messages.

Example:

```text
DISCOVERY
PAIR_REQUEST
PAIR_RESPONSE
KEY_EXCHANGE
SESSION_ESTABLISHED
MESSAGE
DELIVERY_ACK
READ_ACK
DISCONNECT
```

Never place plaintext message content inside discovery or handshake packets.

## 18. Replay Protection

Maintain a record of recently processed message IDs.

If a message with an already processed ID is received:

```text
Ignore duplicate
```

Use authenticated timestamps/nonces/sequence information where appropriate according to the chosen protocol.

## 19. User Interface

Create these screens:

### Home Screen

```text
Encrypted Messenger

Nearby
────────────────

● Alex
  Connected

● Device-21A4
  Available

[ Scan Again ]
```

### Chat Screen

```text
← Alex                     🔒

        Hello!

                   Hey 👋

        Testing offline chat

Type message...       ➤
```

### Settings

```text
Settings

Profile
  Display Name

Security
  Identity Key
  Verify Contacts
  Security Code

Privacy
  Auto-delete Messages
  Hide Message Preview

Connection
  Discovery
  Transport

About
  Version
```

## 20. Permissions

Request only permissions actually required by the selected platform and transport.

Explain permissions clearly.

For example:

```text
Bluetooth Permission

Bluetooth is required to discover
nearby devices running this app.

No internet connection is required.
```

Do not request unrelated permissions.

## 21. Connection Indicator

Display a small status indicator:

```text
🟢 Connected
🟡 Connecting
🔴 Offline
```

Also show:

```text
🔒 Encrypted connection
```

## 22. Testing

Create automated tests for:

### Encryption

* Encrypt/decrypt round trip
* Wrong key fails
* Modified ciphertext fails
* Replay detection
* Key rotation

### Messaging

* Send message
* Receive message
* Message ordering
* Duplicate message handling
* Offline queue
* Reconnection

### Networking

* Device discovery
* Connection
* Disconnection
* Reconnection
* Multiple peers

### Security

Test that:

* Plaintext is never transmitted
* Private keys never leave secure storage
* Tampered packets are rejected
* Unknown peers cannot inject messages
* Replay attacks are rejected

## 23. Debug Mode

Create a developer-only debug screen showing:

```text
Transport:
BLE / Wi-Fi Direct

Connection:
Connected

Peer ID:
XXXXXXXX

Session:
Established

Encryption:
Enabled

Queued Messages:
2
```

Never display:

* Private keys
* Session secrets
* Plaintext message contents in production logs

## 24. Architecture

Use clean modular architecture:

```text
UI
│
├── Chat Screen
├── Device Discovery
├── Settings
└── Security Verification
        │
        ▼
Application Layer
│
├── Chat Manager
├── Peer Manager
├── Message Queue
└── Identity Manager
        │
        ▼
Security Layer
│
├── Identity
├── Key Agreement
├── Session Management
├── Encryption
└── Authentication
        │
        ▼
Transport Layer
│
├── Wi-Fi Direct
├── BLE
└── Platform-specific transport
        │
        ▼
Operating System
```

Keep these layers independent.

## 25. Technology Selection

If the project is Android-only, prefer:

* Kotlin
* Jetpack Compose
* Android Bluetooth/BLE APIs
* Wi-Fi Direct / appropriate Android peer-to-peer APIs
* Room
* Android Keystore
* A reputable cryptographic/messaging library

If cross-platform:

* Choose Flutter or React Native only if the required peer-to-peer APIs can be implemented reliably.
* Put networking functionality in native platform modules where necessary.
* Do not sacrifice transport reliability just to keep everything cross-platform.

Before implementing networking, verify the current platform APIs and restrictions rather than assuming an old API still works.

## 26. Important Offline Limitation

Do not falsely claim that the application can communicate "anywhere with Wi-Fi off."

The phones must have some supported local radio/path available.

The correct promise is:

**No internet required.**

The app can communicate when compatible nearby peer-to-peer connectivity is available, such as Bluetooth or Wi-Fi Direct/local peer networking.

Clearly communicate this to users.

## 27. Deliverables

Generate a complete working project including:

1. Project structure
2. Source code
3. UI
4. Networking layer
5. Peer discovery
6. Secure pairing
7. Encryption
8. Local encrypted message storage
9. Message queue
10. Reconnection
11. Security verification
12. Error handling
13. Unit tests
14. Integration tests
15. README
16. Build instructions
17. Architecture documentation

Do not provide pseudo-code where actual implementation is expected.

If a platform API differs between operating-system versions, implement the correct version-specific behavior.

## 28. Development Order

Build the application in this order:

### Phase 1

Create the UI and navigation.

### Phase 2

Implement device identity and secure key storage.

### Phase 3

Implement peer discovery.

### Phase 4

Implement secure pairing.

### Phase 5

Implement encrypted session establishment.

### Phase 6

Implement one-to-one encrypted messaging.

### Phase 7

Implement message persistence.

### Phase 8

Implement offline queue and reconnection.

### Phase 9

Implement security verification.

### Phase 10

Implement comprehensive testing.

### Phase 11

Perform security review.

### Phase 12

Optimize battery usage and connection reliability.

## 29. Definition of Done

The application is considered complete only when:

* Two phones can install the application.
* They can discover each other without internet.
* They can establish a direct local connection.
* They can authenticate each other.
* They can exchange encrypted messages.
* Plaintext messages never travel over the transport.
* Messages can be stored locally when the peer is temporarily unavailable.
* Messages can be delivered after reconnection.
* Tampered messages are rejected.
* Duplicate/replayed messages are handled safely.
* Private keys remain on the originating device.
* The application works without a cloud backend.
* The UI clearly communicates connection and encryption status.
* The project builds successfully from a clean environment.

## 30. Critical Instruction to the Coding AI

Do not take shortcuts with networking or cryptography.

If a requested feature cannot be implemented reliably on the target platform because of operating-system restrictions, explain the limitation and implement the closest secure alternative.

Do not pretend that a feature works when it does not.

Do not invent cryptographic protocols.

Use established security libraries and protocols wherever possible.

The primary goal is:

**A genuinely offline, peer-to-peer, encrypted messenger where two phones running the same application can exchange text without requiring internet connectivity.**
