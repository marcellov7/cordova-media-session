import AVFoundation
import MediaPlayer

@objc(BackgroundMediaPlugin)
class BackgroundMediaPlugin: CDVPlugin {

    var player: AVPlayer?
    var playerItem: AVPlayerItem?
    var eventCallback: String?
    var playlist: [String: [String: Any]] = [:]
    var playlistOrder: [String] = []
    var currentTrackId: String?

    @objc(play:)
    func play(command: CDVInvokedUrlCommand) {
        guard let urlString = command.arguments[0] as? String,
              let url = URL(string: urlString),
              let title = command.arguments[1] as? String,
              let artist = command.arguments[2] as? String,
              let artworkUrlString = command.arguments[3] as? String,
              let artworkUrl = URL(string: artworkUrlString),
              let isVideo = command.arguments[4] as? Bool,
              let startTime = command.arguments[5] as? Double else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid arguments")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        DispatchQueue.main.async { [weak self] in
            self?.setupAudioSession()

            let asset = AVAsset(url: url)
            self?.playerItem = AVPlayerItem(asset: asset)
            self?.player = AVPlayer(playerItem: self?.playerItem)

            self?.player?.seek(to: CMTime(seconds: startTime, preferredTimescale: 1000))
            self?.player?.play()

            self?.setupRemoteTransportControls()
            self?.setupNowPlaying(title: title, artist: artist, artworkUrl: artworkUrl)

            NotificationCenter.default.addObserver(self, selector: #selector(self?.playerItemDidReachEnd), name: .AVPlayerItemDidPlayToEndTime, object: self?.playerItem)

            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Playback started")
            self?.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }

    @objc(pause:)
    func pause(command: CDVInvokedUrlCommand) {
        DispatchQueue.main.async { [weak self] in
            self?.player?.pause()
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Playback paused")
            self?.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }

    @objc(stop:)
    func stop(command: CDVInvokedUrlCommand) {
        DispatchQueue.main.async { [weak self] in
            self?.player?.pause()
            self?.player?.seek(to: CMTime.zero)
            self?.clearNowPlaying()
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Playback stopped")
            self?.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }

    @objc(setVolume:)
    func setVolume(command: CDVInvokedUrlCommand) {
        guard let volume = command.arguments[0] as? Float else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid volume")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        DispatchQueue.main.async { [weak self] in
            self?.player?.volume = volume
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Volume set to \(volume)")
            self?.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }

    @objc(setPlaybackTime:)
    func setPlaybackTime(command: CDVInvokedUrlCommand) {
        guard let time = command.arguments[0] as? Double else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid time")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        DispatchQueue.main.async { [weak self] in
            self?.player?.seek(to: CMTime(seconds: time, preferredTimescale: 1000))
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Playback time set to \(time)")
            self?.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }

    @objc(setPlaylist:)
    func setPlaylist(command: CDVInvokedUrlCommand) {
        guard let playlistArray = command.arguments[0] as? [[String: Any]] else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid playlist format")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        playlist.removeAll()
        playlistOrder.removeAll()
        for track in playlistArray {
            if let id = track["id"] as? String {
                playlist[id] = track
                playlistOrder.append(id)
            }
        }

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Playlist set successfully")
        commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(addToPlaylist:)
    func addToPlaylist(command: CDVInvokedUrlCommand) {
        guard let track = command.arguments[0] as? [String: Any],
              let id = track["id"] as? String else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid track format")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        playlist[id] = track
        playlistOrder.append(id)

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Track added to playlist")
        commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(removeFromPlaylist:)
    func removeFromPlaylist(command: CDVInvokedUrlCommand) {
        guard let id = command.arguments[0] as? String else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid track ID")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        playlist.removeValue(forKey: id)
        playlistOrder.removeAll { $0 == id }

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Track removed from playlist")
        commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(playById:)
    func playById(command: CDVInvokedUrlCommand) {
        guard let trackId = command.arguments[0] as? String,
              let track = playlist[trackId],
              let urlString = track["url"] as? String,
              let url = URL(string: urlString) else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid track ID or URL")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        setupAudioSession()

        let playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)
        player?.play()

        currentTrackId = trackId
        notifyTrackChanged(trackId: trackId)

        setupRemoteTransportControls()
        setupNowPlaying(track: track)

        // Observe when the current item finishes playing
        NotificationCenter.default.addObserver(self, selector: #selector(playerItemDidReachEnd), name: .AVPlayerItemDidPlayToEndTime, object: playerItem)

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Playback started")
        commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(playNext:)
    func playNext(command: CDVInvokedUrlCommand) {
        guard let currentId = currentTrackId,
              let currentIndex = playlistOrder.firstIndex(of: currentId),
              currentIndex < playlistOrder.count - 1 else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No next track available")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        let nextId = playlistOrder[currentIndex + 1]
        playById(CDVInvokedUrlCommand(arguments: [nextId], callbackId: command.callbackId, className: command.className, methodName: "playById"))
    }

    @objc(playPrevious:)
    func playPrevious(command: CDVInvokedUrlCommand) {
        guard let currentId = currentTrackId,
              let currentIndex = playlistOrder.firstIndex(of: currentId),
              currentIndex > 0 else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "No previous track available")
            commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        let previousId = playlistOrder[currentIndex - 1]
        playById(CDVInvokedUrlCommand(arguments: [previousId], callbackId: command.callbackId, className: command.className, methodName: "playById"))
    }

    @objc func playerItemDidReachEnd(notification: Notification) {
        playNext(CDVInvokedUrlCommand(arguments: [], callbackId: "", className: "", methodName: "playNext"))
    }

    func notifyTrackChanged(trackId: String) {
        guard let callback = eventCallback else { return }
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "onTrackChanged:" + trackId)
        result?.setKeepCallbackAs(true)
        self.commandDelegate.send(result, callbackId: callback)
    }

    private func setupAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [.defaultToSpeaker, .mixWithOthers])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Failed to set audio session category.")
        }
    }

    private func setupRemoteTransportControls() {
        let commandCenter = MPRemoteCommandCenter.shared()

        commandCenter.playCommand.addTarget { [weak self] event in
            self?.player?.play()
            return .success
        }

        commandCenter.pauseCommand.addTarget { [weak self] event in
            self?.player?.pause()
            return .success
        }

        commandCenter.nextTrackCommand.addTarget { [weak self] event in
            self?.playNext(CDVInvokedUrlCommand(arguments: [], callbackId: "", className: "", methodName: "playNext"))
            return .success
        }

        commandCenter.previousTrackCommand.addTarget { [weak self] event in
            self?.playPrevious(CDVInvokedUrlCommand(arguments: [], callbackId: "", className: "", methodName: "playPrevious"))
            return .success
        }

        commandCenter.changePlaybackPositionCommand.addTarget { [weak self] event in
            if let event = event as? MPChangePlaybackPositionCommandEvent {
                self?.player?.seek(to: CMTime(seconds: event.positionTime, preferredTimescale: 1000))
                return .success
            }
            return .commandFailed
        }
    }

    private func setupNowPlaying(track: [String: Any]) {
        var nowPlayingInfo = [String: Any]()
        nowPlayingInfo[MPMediaItemPropertyTitle] = track["title"] as? String
        nowPlayingInfo[MPMediaItemPropertyArtist] = track["artist"] as? String

        if let player = player {
            nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = player.currentTime().seconds
            nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = player.currentItem?.duration.seconds
            nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = player.rate
        }

        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }

    private func clearNowPlaying() {
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
    }

    @objc(registerEventCallback:)
    func registerEventCallback(command: CDVInvokedUrlCommand) {
        self.eventCallback = command.callbackId
    }

    @objc(destroy:)
    func destroy(command: CDVInvokedUrlCommand) {
        DispatchQueue.main.async { [weak self] in
            self?.player?.pause()
            self?.player = nil
            self?.playerItem = nil
            self?.clearNowPlaying()
            NotificationCenter.default.removeObserver(self as Any)
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Plugin destroyed")
            self?.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }
}