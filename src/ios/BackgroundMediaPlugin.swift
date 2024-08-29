import AVFoundation
import MediaPlayer

@objc(BackgroundMediaPlugin)
class BackgroundMediaPlugin: CDVPlugin {
    
    var player: AVPlayer?
    var eventCallback: CDVInvokedUrlCommand?
    
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
            // Stop any existing playback
            self?.player?.pause()
            self?.player = nil
            
            self?.setupAudioSession()
            self?.player = AVPlayer(url: url)
            self?.player?.seek(to: CMTime(seconds: startTime, preferredTimescale: 1000))
            self?.player?.play()
            self?.setupRemoteTransportControls()
            self?.setupNowPlaying(title: title, artist: artist, artworkUrl: artworkUrl, isVideo: isVideo)
            self?.setupPlayerObservers()
            
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
            self?.player = nil
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

    @objc(registerEventCallback:)
    func registerEventCallback(command: CDVInvokedUrlCommand) {
        self.eventCallback = command
    }

    @objc(destroy:)
    func destroy(command: CDVInvokedUrlCommand) {
        DispatchQueue.main.async { [weak self] in
            self?.player?.pause()
            self?.player = nil
            self?.clearNowPlaying()
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Plugin destroyed")
            self?.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }
    
    private func setupAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
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
            self?.sendEvent("onSkipForward", message: "Skipped forward")
            return .success
        }
        
        commandCenter.previousTrackCommand.addTarget { [weak self] event in
            self?.sendEvent("onSkipBackward", message: "Skipped backward")
            return .success
        }
    }
    
    private func setupNowPlaying(title: String, artist: String, artworkUrl: URL, isVideo: Bool) {
        var nowPlayingInfo = [String: Any]()
        nowPlayingInfo[MPMediaItemPropertyTitle] = title
        nowPlayingInfo[MPMediaItemPropertyArtist] = artist
        
        if let player = player {
            nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = player.currentTime().seconds
            nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = player.currentItem?.duration.seconds
            nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = player.rate
        }
        
        // Load artwork asynchronously
        DispatchQueue.global().async {
            if let imageData = try? Data(contentsOf: artworkUrl),
               let image = UIImage(data: imageData) {
                let artwork = MPMediaItemArtwork(boundsSize: image.size) { size in
                    return image
                }
                nowPlayingInfo[MPMediaItemPropertyArtwork] = artwork
                
                DispatchQueue.main.async {
                    MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
                }
            }
        }
        
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }
    
    private func clearNowPlaying() {
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
    }
    
    private func setupPlayerObservers() {
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(playerItemDidReachEnd),
                                               name: .AVPlayerItemDidPlayToEndTime,
                                               object: player?.currentItem)
    }
    
    @objc private func playerItemDidReachEnd() {
        sendEvent("onPlaybackEnd", message: "Playback ended")
    }
    
    private func sendEvent(_ event: String, message: String) {
        guard let callback = eventCallback else { return }
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "\(event): \(message)")
        result?.setKeepCallbackAs(true)
        self.commandDelegate.send(result, callbackId: callback.callbackId)
    }
}