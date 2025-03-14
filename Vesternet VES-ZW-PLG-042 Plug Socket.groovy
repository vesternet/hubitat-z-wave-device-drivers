/**
 *    Vesternet VES-ZW-PLG-042 Plug Socket
 */
metadata {
    definition(name: 'Vesternet VES-ZW-PLG-042 Plug Socket', namespace: 'Vesternet', author: 'Vesternet', importUrl: 'https://raw.githubusercontent.com/vesternet/hubitat-z-wave-device-drivers/main/Vesternet%20VES-ZW-PLG-042%20Plug%20Socket.groovy') {
        capability 'Switch'
        capability 'Actuator'
        capability 'Sensor'
        capability 'PowerMeter'
        capability 'EnergyMeter'
        capability 'VoltageMeasurement'
        capability 'CurrentMeter'
        capability 'Refresh'
        capability 'Configuration'

        fingerprint mfr: '0260', prod: '8006', deviceId: '1000', inClusters:'0x5E,0x6C,0x55,0x98,0x9F', deviceJoinName: 'Vesternet VES-ZW-PLG-042 Plug Socket'
        fingerprint mfr: '0260', prod: '8006', deviceId: '1000', inClusters:'0x5E,0x25,0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x73,0x32,0x70,0x98,0x9F,0x6C,0x7A', deviceJoinName: 'Vesternet VES-ZW-PLG-042 Plug Socket'
    }
    preferences {
        input name: 'powerFailState', type: 'enum', title: 'Load State After Power Failure', options: [0: 'previous state', 1: 'on', 2: 'off'], defaultValue: 0
        input name: 'ledState', type: 'enum', title: 'State Of The LED', options: [0: 'normal', 1: 'opposite', 2: 'permanently on', 3: 'permanently off'], defaultValue: 0
        input name: 'powerReportChangeWatts', type: 'number', title: 'Power Change Watts: 1W - 255W<br>1 = 1W, 5 = 5W, etc, 0 = disabled, default = 10 (10W)', range: '0..255', defaultValue: 10
        input name: 'voltageReportChangeVolts', type: 'number', title: 'Voltage Change Volts: 0.1V - 25.5V <br>1 = 0.1V, 10 = 1V, etc, 0 = disabled, default = 10 (1V)', range: '0..255', defaultValue: 10
        input name: 'currentReportChangeAmps', type: 'number', title: 'Current Change Amps: 0.1A - 25.5A<br>1 = 0.1A, 10 = 1A, etc, 0 = disabled, default = 1 (0.1A)', range: '0..255', defaultValue: 1
        input name: 'energyReportChangekWh', type: 'number', title: 'Energy Change kWh: 0.001kWh - 1.024kWh<br>1 = 0.001kWh, 500 = 0.5kWh, etc, 0 = disabled, default = 500 (0.5kWh)', range: '0..1024', defaultValue: 500
        input name: 'reportTime', type: 'enum', title: 'Power, Voltage, Current & Energy Report Time (s)', options: [0: 'disabled', 60:'60s', 90:'90s', 120:'120s', 240:'240s', 300:'300s', 600:'600s', 1200:'1200s', 1800:'1800s', 3600:'3600s', 7200:'7200s'], defaultValue: 1800
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText Logging', defaultValue: true
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', defaultValue: true
    }
}

def getCommandClassVersions() {
    [ 0x20: 1, 0x25: 1, 0x32: 4, 0x55: 2, 0x59: 1, 0x5A: 1, 0x5E: 2, 0x6C: 1, 0x72: 2, 0x73: 1, 0x7A: 4, 0x85: 2, 0x86: 3, 0x8E: 3, 0x98: 1, 0x9F: 1 ]
}

def installed() {
    device.updateSetting('logEnable', [value: 'true', type: 'bool'])
    logDebug('installed called')
    device.updateSetting('powerFailState', [value: '0', type: 'enum'])
    device.updateSetting('ledState', [value: '0', type: 'enum'])
    device.updateSetting('powerReportChangeWatts', [value: 10, type: 'number'])
    device.updateSetting('voltageReportChangeVolts', [value: 10, type: 'number'])
    device.updateSetting('currentReportChangeAmps', [value: 1, type: 'number'])
    device.updateSetting('energyReportChangekWh', [value: 500, type: 'number'])
    device.updateSetting('reportTime', [value: '1800', type: 'enum'])
    runIn(1800, logsOff)
}

def updated() {
    logDebug('updated called')
    log.warn("debug logging is: ${logEnable == true}")
    log.warn("power fail state is: ${powerFailState == '0' ? 'previous state' : powerFailState == '1' ? 'on' : 'off'}")
    log.warn("led state is: ${ledState == '0' ? 'normal' : ledState == '1' ? 'opposite' : ledState == '2' ? 'permanently on' : 'permanently off'}")
    log.warn("power report change watts is: ${powerReportChangeWatts}W")
    log.warn("voltage report change volts is:${voltageReportChangeVolts == 0 ?: voltageReportChangeVolts / 10}V")
    log.warn("current report change amps is: ${currentReportChangeAmps == 0 ?: currentReportChangeAmps / 10}A")
    log.warn("energy report change amps is: ${energyReportChangekWh == 0 ?: energyReportChangekWh / 1000}kWh")
    log.warn("power, voltage, current & energy report time is: ${reportTime}s")
    state.clear()
    unschedule()
    if (logEnable) runIn(1800, logsOff)
}

def configure() {
    logDebug('configure called')
    def cmds = commands([
                        zwave.configurationV1.configurationGet(parameterNumber: 7),
                        zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, scaledConfigurationValue: powerFailState.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 7),
                        zwave.configurationV1.configurationGet(parameterNumber: 6),
                        zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, scaledConfigurationValue: ledState.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 6),
                        zwave.configurationV1.configurationGet(parameterNumber: 4),
                        zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: powerReportChangeWatts.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 4),
                        zwave.configurationV1.configurationGet(parameterNumber: 3),
                        zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: currentReportChangeAmps.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 3),
                        zwave.configurationV1.configurationGet(parameterNumber: 2),
                        zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: voltageReportChangeVolts.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 2),
                        zwave.configurationV1.configurationGet(parameterNumber: 5),
                        zwave.configurationV1.configurationSet(parameterNumber: 5, size: 4, scaledConfigurationValue: energyReportChangekWh.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 5),
                        zwave.configurationV1.configurationGet(parameterNumber: 1),
                        zwave.configurationV1.configurationSet(parameterNumber: 1, size: 4, scaledConfigurationValue: reportTime.toInteger()),
                        zwave.configurationV1.configurationGet(parameterNumber: 1),
                        ],
                        1000)
    logDebug("sending ${cmds}")
    return cmds
}

def refresh() {
    logDebug('refresh called')
    def cmds = commands([
                        zwave.switchBinaryV1.switchBinaryGet(),
                        zwave.meterV4.meterGet(scale: 0),
                        zwave.meterV4.meterGet(scale: 2),
                        zwave.meterV4.meterGet(scale: 4),
                        zwave.meterV4.meterGet(scale: 5)
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
    logDebug('hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation called')
    logDebug("got cmd: ${cmd}")
    hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand)
    }
    else {
        log.warn("Unable to extract encapsulated cmd from ${cmd}")
    }
}

def zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd) {
    logDebug('zwaveEvent hubitat.zwave.commands.supervisionv1.SupervisionGet called')
    logDebug("got cmd: ${cmd}")
    hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand)
    }
    else {
        log.warn("Unable to extract encapsulated cmd from ${cmd}")
    }
    sendHubCommand(new hubitat.device.HubAction(commands(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0)), hubitat.device.Protocol.ZWAVE))
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    logDebug('zwaveEvent hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport called')
    logDebug("got cmd: ${cmd}")
    def switchValue = cmd.value == 0 ? 'off' : 'on'
    def descriptionText = "${device.displayName} was turned ${switchValue}"
    if (device.currentValue('switch') && switchValue == device.currentValue('switch')) {
        descriptionText = "${device.displayName} is ${switchValue}"
    }
    def type = 'physical'
    if (state['action'] == 'digitalon' || state['action'] == 'digitaloff') {
        logDebug("action is ${state['action']}")
        type = 'digital'
        state['action'] = 'standby'
    }
    logText(descriptionText)
    sendEvent(getEvent([name: 'switch', value: switchValue, type: type, descriptionText: descriptionText]))
}

def zwaveEvent(hubitat.zwave.commands.meterv4.MeterReport cmd) {
    logDebug('zwaveEvent hubitat.zwave.commands.meterv4.MeterReport called')
    logDebug("got: ${cmd}")
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

def zwaveEvent(hubitat.zwave.Command cmd) {
    logDebug('zwaveEvent hubitat.zwave.Command called')
    log.warn("skipped cmd: ${cmd}")
}

def on() {
    logDebug('on called')
    def cmds = commands(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF))
    logDebug("sending ${cmds}")
    state['action'] = 'digitalon'
    return cmds
}

def off() {
    logDebug('off called')
    def cmds = commands(zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00))
    logDebug("sending ${cmds}")
    state['action'] = 'digitaloff'
    return cmds
}

def getEvent(event) {
    logDebug("getEvent called data: ${event}")
    return createEvent(event)
}

def commands(java.util.ArrayList cmds, delay = 200) {
    return delayBetween(cmds.collect { secure(it) }, delay)
}

def commands(hubitat.zwave.Command cmd) {
    return secure(cmd)
}

def commands(String cmd) {
    return secure(cmd)
}

def secure(String cmd) {
    logDebug("secure called cmd: ${cmd}")
    def encapCmd = zwaveSecureEncap(cmd)
    logDebug("returns cmd: ${encapCmd}")
    return encapCmd
}

def secure(hubitat.zwave.Command cmd) {
    logDebug("secure called cmd: ${cmd}")
    def encapCmd = zwaveSecureEncap(cmd)
    logDebug("returns cmd: ${encapCmd}")
    return encapCmd
}

def secure(java.util.ArrayList cmds) {
    return cmds.collect { secure(it) }
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
