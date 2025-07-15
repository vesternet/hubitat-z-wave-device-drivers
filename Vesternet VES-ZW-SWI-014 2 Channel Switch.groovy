/**
 *    Vesternet VES-ZW-SWI-014 2 Channel Switch
 */
metadata {
    definition(name: 'Vesternet VES-ZW-SWI-014 2 Channel Switch', namespace: 'Vesternet', author: 'Vesternet', importUrl: 'https://raw.githubusercontent.com/vesternet/hubitat-z-wave-device-drivers/main/Vesternet%20VES-ZW-SWI-014%202%20Channel%20Switch.groovy', singleThreaded: true) {
        capability 'Switch'
        capability 'Actuator'
        capability 'Sensor'
        capability 'PowerMeter'
        capability 'EnergyMeter'
        capability 'VoltageMeasurement'
        capability 'CurrentMeter'
        capability 'PushableButton'
        capability 'DoubleTapableButton'
        capability 'HoldableButton'
        capability 'ReleasableButton'
        capability 'Refresh'
        capability 'Configuration'

        command 'tripleTap', [ [ name:'Triple Tap*', type: 'NUMBER', description: 'Button number to triple tap', required: true ] ]

        attribute 'deviceNotification', 'string'

        fingerprint mfr: '0330', prod: '0004', deviceId: 'D109', inClusters:'0x5E,0x55,0x98,0x9F,0x6C', deviceJoinName: 'Vesternet VES-ZW-SWI-014 2 Channel Switch'
    }
    preferences {
        input name: 'binaryReport', type: 'enum', title: 'Send Binary Report', options: [0: 'disabled', 1: 'enabled'], defaultValue: 1
        input name: 'powerFailState', type: 'enum', title: 'Load State After Power Failure', options: [0: 'off', 1: 'on', 2: 'previous state'], defaultValue: 2
        input name: 'overCurrentProtection', type: 'enum', title: 'Overcurrent Protection (over 16A or 3700W)', options: [0: 'disabled', 1: 'enabled'], defaultValue: 1
        input name: 'localSwitchControlOption', type: 'number', title: 'Configuration Of Switch Control (Mode 0 - 4, 0 = default, see manual for options)', range: '0..4', defaultValue: 0
        input name: 'switchInclusion', type: 'enum', title: 'Allow Switch Input To Include / Exclude', options: [0: 'disabled', 1: 'enabled'], defaultValue: 1
        input name: 'centralSceneNotification', type: 'enum', title: 'Central Scene Configuration', options: [0: 'disabled', 1: 'enabled for both inputs', 2: 'enabled for input S1', 3: 'enabled for input S2'], defaultValue: 1
        input name: 'powerReportChangeWatts', type: 'number', title: 'Power Change Watts (1W - 255W, 0 = disabled)', range: '0..255', defaultValue: 5
        input name: 'voltageReportChangeVolts', type: 'number', title: 'Voltage Change Volts (1V - 255V, 0 = disabled)', range: '0..255', defaultValue: 2
        input name: 'currentReportChangeAmps', type: 'number', title: 'Current Change Amps (0.1A - 25.5A, 0 = disabled)', range: '0..255', defaultValue: 1
        input name: 'energyReportTime', type: 'enum', title: 'Energy Time (s)', options: [0:'disabled', 10:'10s', 20:'20s', 30:'30s', 40:'40s', 50:'50s', 60:'60s', 90:'90s', 120:'120s', 240:'240s', 300:'300s', 600:'600s', 1200:'1200s', 1800:'1800s', 3600:'3600s', 7200:'7200s'], defaultValue: 1800
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText Logging', defaultValue: true
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', defaultValue: true
    }
}

def getCommandClassVersions() {
    [ 0x20: 1, 0x25: 1, 0x32: 3, 0x60: 4, 0x70: 1, 0x71: 8, 0x5B: 3 ]
}

def getModelNumberOfButtons() {
    logDebug('getModelNumberOfButtons called')
    ['53513' : 2]
}

def installed() {
    device.updateSetting('logEnable', [value: 'true', type: 'bool'])
    device.updateSetting('txtEnable', [value: 'true', type: 'bool'])
    logDebug('installed called')
    device.updateSetting('binaryReport', [value: '1', type: 'enum'])
    device.updateSetting('powerFailState', [value: '2', type: 'enum'])
    device.updateSetting('overCurrentProtection', [value: '1', type: 'enum'])
    device.updateSetting('localSwitchControlOption', [value: 0, type: 'number'])
    device.updateSetting('switchInclusion', [value: '1', type: 'enum'])
    device.updateSetting('centralSceneNotification', [value: '1', type: 'enum'])
    device.updateSetting('powerReportChangeWatts', [value: 5, type: 'number'])
    device.updateSetting('voltageReportChangeVolts', [value: 2, type: 'number'])
    device.updateSetting('currentReportChangeAmps', [value: 1, type: 'number'])
    device.updateSetting('energyReportTime', [value: '1800', type: 'enum'])
    def numberOfButtons = modelNumberOfButtons[device.getDataValue('deviceId')]
    logDebug("numberOfButtons: ${numberOfButtons}")
    sendEvent(getEvent(name: 'numberOfButtons', value: numberOfButtons, displayed: false))
    for(def buttonNumber : 1..numberOfButtons) {
        sendEvent(getButtonEvent('pushed', buttonNumber, 'digital'))
    }
    runIn(1800, logsOff)
}

def updated() {
    logDebug('updated called')
    log.warn("debug logging is: ${logEnable == true}")
    log.warn("descriptionText logging is: ${txtEnable == true}")
    log.warn("binary report is: ${binaryReport == '0' ? 'disabled' : 'enabled'}")
    log.warn("power fail state is: ${powerFailState == '0' ? 'off' : powerFailState == '1' ? 'on' : 'previousstate'}")
    log.warn("overcurrent protection is: ${overCurrentProtection == '0' ? 'disabled' : 'enabled'}")
    log.warn("local switch control option is: ${localSwitchControlOption}")
    log.warn("switch inclusion is: ${switchInclusion == '0' ? 'disabled' : 'enabled'}")
    log.warn("central scene notification is: ${centralSceneNotification}")
    log.warn("power report change watts is: ${powerReportChangeWatts}W")
    log.warn("voltage report change volts is: ${voltageReportChangeVolts}V")
    log.warn("current report change amps is: ${currentReportChangeAmps == 0 ?: currentReportChangeAmps / 10}A")
    log.warn("energy report time is: ${energyReportTime}s")
    state.clear()
    unschedule()
    if (logEnable) runIn(1800, logsOff)
}

def configure() {
    logDebug('configure called')
    def cmds = commands([
                        zwave.configurationV1.configurationGet(parameterNumber: 2),
                        zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: binaryReport.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 2),
                        zwave.configurationV1.configurationGet(parameterNumber: 4),
                        zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: powerFailState.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 4),
                        zwave.configurationV1.configurationGet(parameterNumber: 5),
                        zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: overCurrentProtection.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 5),
                        zwave.configurationV1.configurationGet(parameterNumber: 7),
                        zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: localSwitchControlOption.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 7),
                        zwave.configurationV1.configurationGet(parameterNumber: 8),
                        zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: switchInclusion.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 8),
                        zwave.configurationV1.configurationGet(parameterNumber: 9),
                        zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: centralSceneNotification.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 9),
                        zwave.configurationV1.configurationGet(parameterNumber: 10),
                        zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: powerReportChangeWatts.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 10),
                        zwave.configurationV1.configurationGet(parameterNumber: 11),
                        zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, scaledConfigurationValue: currentReportChangeAmps.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 11),
                        zwave.configurationV1.configurationGet(parameterNumber: 12),
                        zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1, scaledConfigurationValue: voltageReportChangeVolts.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 12),
                        zwave.configurationV1.configurationGet(parameterNumber: 14),
                        zwave.configurationV1.configurationSet(parameterNumber: 14, size: 4, scaledConfigurationValue: energyReportTime.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 14)
                        ],
                        1000)
    logDebug("sending ${cmds}")
    return cmds
}

def refresh() {
    logDebug('refresh called')
    def cmds = commands([
                        ['cmd': zwave.switchBinaryV2.switchBinaryGet(), 'endpoint': 1],
                        ['cmd': zwave.switchBinaryV2.switchBinaryGet(), 'endpoint': 2],
                        zwave.meterV3.meterGet(scale: 0),
                        zwave.meterV3.meterGet(scale: 2),
                        zwave.meterV3.meterGet(scale: 4),
                        zwave.meterV3.meterGet(scale: 5)
                        ],
                        500)
    logDebug("sending ${cmds}")
    return cmds
}

def parse(String description) {
    logDebug('parse called')
    logDebug("got description: ${description}")
    def cmd = zwave.parse(description, commandClassVersions)
    if (cmd) {
        zwaveEvent(cmd)
    }
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    zwaveEvent(cmd, 0)
}

def zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd, endpoint) {
    logDebug('hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation called')
    logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
    hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand, endpoint)
    }
    else {
        log.warn("Unable to extract encapsulated cmd from ${cmd}")
    }
}

def zwaveEvent(hubitat.zwave.commands.multichannelv4.MultiChannelCmdEncap cmd) {
    logDebug('hubitat.zwave.commands.multichannelv4.MultiChannelCmdEncap called')
    logDebug("got cmd: ${cmd}")
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        logDebug("got encapsulatedCommand: ${encapsulatedCommand} from endpoint: ${cmd.sourceEndPoint}")
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
    }
    else {
        log.warn("Unable to extract encapsulated cmd from ${cmd}")
    }
}

def zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd){
    zwaveEvent(cmd, 0)
}

def zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd, endpoint) {
    logDebug('zwaveEvent hubitat.zwave.commands.supervisionv1.SupervisionGet called')
    logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
    hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand, endpoint)
    }
    else {
        log.warn("Unable to extract encapsulated cmd from ${cmd}")
    }
    if (endpoint > 0) {
        sendHubCommand(new hubitat.device.HubAction(commands(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0), endpoint), hubitat.device.Protocol.ZWAVE))
    }
    else {
        sendHubCommand(new hubitat.device.HubAction(commands(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0)), hubitat.device.Protocol.ZWAVE))
    }
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    zwaveEvent(cmd, 0)
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint) {
    logDebug('zwaveEvent hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport called')
    logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
    if (endpoint == 1 || endpoint == 2) {
        def onOffState = cmd.value == 0 ? 'off' : 'on'
        def descriptionText = "was turned ${onOffState}"
        def currentChildValue = getChildDeviceCurrentValue("EP${endpoint}")
        if (onOffState == currentChildValue) {
            descriptionText = "is ${onOffState}"
        }
        def type = 'physical'
        def action = state["action-EP${endpoint}"] ?: 'standby'
        if (action == 'digitalon' || action == 'digitaloff') {
            logDebug("action is ${action}")
            type = 'digital'
            state["action-EP${endpoint}"] = 'standby'
            logDebug('action set to standby')
        }
        sendEventToChildDevice("EP${endpoint}", 'switch', onOffState, descriptionText, ['type': type])
        def currentValue = device.currentValue('switch')
        if (getChildDeviceCurrentValue("EP${endpoint == '1' ? '2' : '1'}") == onOffState) {
            logDebug('both child devices states match')
            descriptionText = "${device.displayName} was turned ${onOffState}"
            if (onOffState == currentValue) {
                descriptionText = "${device.displayName} is ${onOffState}"
            }
            logText(descriptionText)
            sendEvent(getEvent([name: 'switch', value: onOffState, descriptionText: descriptionText]))
        }
    }
    else {
        logDebug('got command from unexpected endpoint, skipping!')
    }
}

def zwaveEvent(hubitat.zwave.commands.centralscenev3.CentralSceneNotification cmd){
    zwaveEvent(cmd, 0)
}

def zwaveEvent(hubitat.zwave.commands.centralscenev3.CentralSceneNotification cmd, endpoint){
    logDebug('zwaveEvent hubitat.zwave.commands.centralscenev3.CentralSceneNotification called')
    logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
    if (endpoint == 0) {
        Integer button = cmd.sceneNumber
        Integer key = cmd.keyAttributes
        String action
        switch (key) {
            case 0:
                action = 'pushed'
                break
            case 1:
                action = 'released'
                break
            case 2:
                action = 'held'
                break
            case 3:
                action = 'doubleTapped'
                break
            case 4:
                action = 'tripleTapped'//not supported on hubitat capability list
                break
            default:
                log.warn("skipped cmd: ${cmd}")
                break
        }
        if (action){
            sendEvent(getButtonEvent(action, button, 'physical'))
        }
    }
    else {
        logDebug('got command from unexpected endpoint, skipping!')
    }
}

def zwaveEvent(hubitat.zwave.commands.meterv3.MeterReport cmd) {
    zwaveEvent(cmd, 0)
}

def zwaveEvent(hubitat.zwave.commands.meterv3.MeterReport cmd, endpoint) {
    logDebug('zwaveEvent hubitat.zwave.commands.meterv3.MeterReport called')
    logDebug("got cmd: ${cmd} from endpoint: ${endpoint}")
    if (endpoint == 0) {
        def descriptionText = ''
        if (cmd.meterType == 1) {
            if (cmd.scale == 0) {
                logDebug("energy report is ${cmd.scaledMeterValue} kWh")
                descriptionText = "${device.displayName} is set to ${cmd.scaledMeterValue}kWh"
                sendEvent(getEvent(name: 'energy', value: cmd.scaledMeterValue, unit: 'kWh', descriptionText: descriptionText))
            }
            else if (cmd.scale == 2) {
                logDebug("power report is ${cmd.scaledMeterValue} W")
                descriptionText = "${device.displayName} is set to ${cmd.scaledMeterValue}W"
                sendEvent(getEvent(name: 'power', value: cmd.scaledMeterValue, unit: 'W', descriptionText: descriptionText))
            }
            else if (cmd.scale == 4) {
                logDebug("voltage report is ${cmd.scaledMeterValue} V")
                descriptionText = "${device.displayName} is set to ${cmd.scaledMeterValue}V"
                sendEvent(getEvent(name: 'voltage', value: cmd.scaledMeterValue, unit: 'V', descriptionText: descriptionText))
            }
            else if (cmd.scale == 5) {
                logDebug("current report is ${cmd.scaledMeterValue} A")
                descriptionText = "${device.displayName} is set to ${cmd.scaledMeterValue}A"
                sendEvent(getEvent(name: 'amperage', value: cmd.scaledMeterValue, unit: 'A', descriptionText: descriptionText))
            }
            else {
                log.warn("skipped cmd: ${cmd}")
            }
            if (descriptionText != '') {
                logText(descriptionText)
            }
        }
    }
    else {
        logDebug('got command from unexpected endpoint, skipping!')
    }
}

def zwaveEvent(hubitat.zwave.commands.notificationv8.NotificationReport cmd) {
    logDebug('zwaveEvent hubitat.zwave.commands.notificationv8.NotificationReport called')
    logDebug("got cmd: ${cmd}")
    def deviceNotification = ''
    if (cmd.notificationType == 8) {
        logDebug("got power management notification event: ${cmd.event}")
        switch (cmd.event) {
            case 6:
                // overcurrent detected
                deviceNotification = 'current exceeds device limit, emergency shutoff triggered!'
                log.warn(deviceNotification)
                break
            case 8:
                // overload detected
                deviceNotification = 'load exceeds device limit, emergency shutoff triggered!'
                log.warn(deviceNotification)
                break
            default:
                log.warn("skipped cmd: ${cmd}")
        }
    }
    else {
        log.warn("skipped cmd: ${cmd}")
    }
    if (deviceNotification != '') {
        def descriptionText = "${device.displayName} raised notifiction - ${deviceNotification}"
        sendEvent(getEvent(name: 'deviceNotification', value: deviceNotification, descriptionText: descriptionText))
    }
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    logDebug('zwaveEvent hubitat.zwave.Command called')
    log.warn("skipped cmd: ${cmd}")
}

def on() {
    logDebug('on called')
    def cmds = commands([['cmd': zwave.switchBinaryV2.switchBinarySet(switchValue: 0xFF), 'endpoint': 1], ['cmd': zwave.switchBinaryV2.switchBinarySet(switchValue: 0xFF), 'endpoint': 2]])
    logDebug("sending ${cmds}")
    state['action-EP1'] = 'digitalon'
    state['action-EP2'] = 'digitalon'
    return cmds
}

def off() {
    logDebug('off called')
    def cmds = commands([['cmd': zwave.switchBinaryV2.switchBinarySet(switchValue: 0x00), 'endpoint': 1], ['cmd': zwave.switchBinaryV2.switchBinarySet(switchValue: 0x00), 'endpoint': 2]])
    logDebug("sending ${cmds}")
    state['action-EP1'] = 'digitaloff'
    state['action-EP2'] = 'digitaloff'
    return cmds
}

def componentOn(childDevice) {
    logDebug('componentOn called')
    logDebug("got childDevice: ${childDevice.displayName}")
    def endpoint = childDevice.deviceNetworkId.split('-EP')[1]
    def cmds = commands(zwave.switchBinaryV2.switchBinarySet(switchValue: 0xFF), endpoint)
    logDebug("sending ${cmds}")
    state["action-EP${endpoint}"] = 'digitalon'
    sendHubCommand(new hubitat.device.HubAction(cmds, hubitat.device.Protocol.ZWAVE))
}

def componentOff(childDevice) {
    logDebug('componentOff called')
    logDebug("got childDevice: ${childDevice.displayName}")
    def endpoint = childDevice.deviceNetworkId.split('-EP')[1]
    def cmds = commands(zwave.switchBinaryV2.switchBinarySet(switchValue: 0x00), endpoint)
    logDebug("sending ${cmds}")
    state["action-EP${endpoint}"] = 'digitaloff'
    sendHubCommand(new hubitat.device.HubAction(cmds, hubitat.device.Protocol.ZWAVE))
}

def componentRefresh(childDevice) {
    logDebug('componentRefresh called')
    logDebug("got childDevice: ${childDevice.displayName}")
    def endpoint = childDevice.deviceNetworkId.split('-EP')[1]
    def cmds = commands(zwave.switchBinaryV2.switchBinaryGet(), endpoint)
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubAction(cmds, hubitat.device.Protocol.ZWAVE))
}

def sendEventToChildDevice(address, event, attributeValue, childDescriptionText, options = [:]) {
    logDebug("sendEventToChildDevice called address: ${address} event: ${event} attributeValue: ${attributeValue} descriptionText: ${childDescriptionText} options: ${options}")
    def childDevice = this.getChildDevice("${device.id}-${address}")
    if (childDevice == null) {
        logDebug("creating child device for address: ${address}")
        this.addChildDevice('Vesternet', 'Vesternet VES-ZW-SWI-014 2 Channel Switch Child Switch', "${device.id}-${address}", [name: "${device.displayName} ${address}", label: "${device.displayName} ${address}", isComponent: true])
        childDevice = this.getChildDevice("${device.id}-${address}")
    }
    if (childDevice) {
        logDebug("got child device name: ${childDevice.name} displayName: ${childDevice.displayName}, sending event")
        descriptionText = "${childDevice.displayName} ${childDescriptionText}"
        def childEvent = [name: event, value: attributeValue, descriptionText: descriptionText]
        if (options.type) {
            childEvent.type = options.type
        }
        if (options.unit) {
            childEvent.unit = options.unit
        }
        childDevice.parse([getEvent(childEvent)])
    }
    else {
        log.warn('could not find child device, skipping event!')
    }
}

def getChildDeviceCurrentValue(address) {
    logDebug("getChildDeviceCurrentValue called address: ${address}")
    def currentValue = 'unknown'
    def childDevice = this.getChildDevice("${device.id}-${address}")
    if (childDevice == null) {
        logDebug("creating child device for address: ${address}")
        this.addChildDevice('Vesternet', 'Vesternet VES-ZW-SWI-014 2 Channel Switch Child Switch', "${device.id}-${address}", [name: "${device.displayName} ${address}", label: "${device.displayName} ${address}", isComponent: true])
        childDevice = this.getChildDevice("${device.id}-${address}")
    }
    if (childDevice) {
        logDebug("got child device name: ${childDevice.name} displayName: ${childDevice.displayName}, getting state")
            currentValue = childDevice.currentValue('switch') ?: 'unknown'
            logDebug("got currentValue: ${currentValue}")
            return currentValue
    }
    else {
        log.warn('could not find child device!')
    }
    return currentValue
}

def push(button){
    logDebug('push called')
    sendEvent(getButtonEvent('pushed', button, 'digital'))
}

def hold(button){
    logDebug('hold called')
    sendEvent(getButtonEvent('held', button, 'digital'))
}

def release(button){
    logDebug('release called')
    sendEvent(getButtonEvent('released', button, 'digital'))
}

def doubleTap(button){
    logDebug('doubleTap called')
    sendEvent(getButtonEvent('doubleTapped', button, 'digital'))
}

def tripleTap(button){
    logDebug('tripleTap called')
    sendEvent(getButtonEvent('tripleTapped', button, 'digital'))
}

def getButtonEvent(action, button, type) {
    logDebug("getButtonEvent called button: ${button} action: ${action} type: ${type} ")
    def descriptionText = "${device.displayName} button ${button} is ${action}"
    logText(descriptionText)
    return getEvent(name: action, value: button, descriptionText: descriptionText, isStateChange: true, type: type)
}

def getEvent(event) {
    logDebug("getEvent called data: ${event}")
    return createEvent(event)
}

def commands(java.util.ArrayList cmds, delay = 200, endpoint = null) {
    return delayBetween(cmds.collect {
        if (it instanceof String || it instanceof hubitat.zwave.Command) {
            secure(it, endpoint)
        }
        else {
            secure(it.cmd, it.endpoint)
        }
        }, delay)
}

def commands(hubitat.zwave.Command cmd, endpoint = null) {
    return secure(cmd, endpoint)
}

def commands(String cmd, endpoint = null) {
    return secure(cmd, endpoint)
}

def secure(String cmd, endpoint = null){
    logDebug("secure called cmd: ${cmd} endpoint: ${endpoint}")
    if (endpoint) {
        if (endpoint instanceof String){
            endpoint = endpoint.toInteger()
        }
        cmd = zwave.multiChannelV4.multiChannelCmdEncap(sourceEndPoint: 0, bitAddress: 0, res01:0, destinationEndPoint: endpoint).encapsulate(cmd)
    }
    def encapCmd = zwaveSecureEncap(cmd)
    logDebug("returns cmd: ${encapCmd}")
    return encapCmd
}

def secure(hubitat.zwave.Command cmd, endpoint = null){
    logDebug("secure called cmd: ${cmd} endpoint: ${endpoint}")
    if (endpoint) {
        if (endpoint instanceof String){
            endpoint = endpoint.toInteger()
        }
        cmd = zwave.multiChannelV4.multiChannelCmdEncap(sourceEndPoint: 0, bitAddress: 0, res01:0, destinationEndPoint: endpoint).encapsulate(cmd)
    }
    def encapCmd = zwaveSecureEncap(cmd)
    logDebug("returns cmd: ${encapCmd}")
    return encapCmd
}

def logDebug(msg) {
    if (logEnable != false) {
        log.debug("${msg}")
    }
}

def logText(msg) {
    if (txtEnable != false) {
        log.info("${msg}")
    }
}

def logsOff() {
    log.warn('debug logging disabled')
    device.updateSetting('logEnable', [value:'false', type: 'bool'])
}
