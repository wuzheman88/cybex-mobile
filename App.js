/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */
import React, {Component} from 'react';
import {
    Alert,
    Platform,
    StyleSheet,
    Button,
    Text,
    TextInput,
    View
} from 'react-native';
import {run} from "./lib/btc";
import {ethTest} from "./lib/eth";
import {cybexDaemon} from "./lib/cybex";

const instructions = Platform.select({
    ios: 'Press Cmd+R to reload,\nCmd+D or shake for dev menu',
    android: 'Double tap R on your keyboard to reload,\nShake or press menu button for dev m' +
            'enu'
});

export default class App extends Component {
    constructor(props) {
        super(props);
        cybexDaemon.init();
        this.state = {
            to: ""
        }
    }

    _testBTC = () => {
        run();
        Alert.alert("Test Result", "Btc Test Done!");        
    }
    _testETH = () => {
        ethTest();
        Alert.alert("Test Result", "Eth Test Done!");
    }

    _transfer = async () => {
        let toAccount;
        if (/1\.2\..+/.test(this.state.to)) {
            toAccount = this.state.to;
        } else {
            toAccount = await cybexDaemon.getAccountByName(this.state.to);
            toAccount = toAccount.id;
        }
        if (!toAccount) {
            Alert.alert(
                "Unknown Account",
                `Can't find account ${this.state.to} on Cybex Chain`
            );
        }
        console.debug("Transfer : ", Number(this.state.amount * 100000));
        await cybexDaemon.performTransfer(
            {
                to_account: toAccount, 
                amount: parseInt(Number(this.state.amount * 100000)), 
                asset: "1.3.0", 
                memo: "From Mobile Client"
            }
        );
        Alert.alert("Transfer Result", "Transfer Done. Check your account!");
    }

    _validate = () => {
        let {to} = this.state;
        let toValid = /^.+/.test(to);
        let amountValid = !isNaN(Number(this.state.amount));
        return toValid && amountValid;
    }

    render() {
        let transferEnabled = this._validate();

        return (
            <View style={styles.container}>
                <Text style={styles.welcome}>
                    Welcome to Cybex transfer test!
                </Text>
                <Text style={styles.instructions}>
                    Transfer To:
                </Text>
                <TextInput 
                autoCapitalize="none"
                style={styles.textInput} 
                onChangeText={to => this.setState({to})}/>
                <Text style={styles.instructions}>
                    Amount:
                </Text>
                <TextInput
                    style={styles.textInput}
                    onChangeText={amount => this.setState({
                        amount
                    })}/>
                <Text style={styles.instructions}>
                    {instructions}
                </Text>
                <Button
                    disabled={!transferEnabled}
                    onPress={this._transfer}
                    title="Go transfer!"/>
                <Button onPress={this._testBTC} title="Test BTC"/>
                <Button onPress={this._testETH} title="Test ETH"/>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F5FCFF'
    },
    welcome: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10
    },
    textInput: {
        // flex: 1,
        borderWidth: 1,
        width: "100%"
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5
    }
});
