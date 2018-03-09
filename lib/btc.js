const bitcoin = require('bitcoinjs-lib')

const wif = require('wif')
const bip38 = require('bip38')
const bip39 = require('bip39')

function genRandomAddress() {
  var keyPair = bitcoin.ECPair.makeRandom({ rng: () => { return Buffer.from('zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz') } })
  var address = keyPair.getAddress()
  console.log(address == '1F5VhMHukdnUES9kfXqzPzMeF1GPHKiF64')
}

function derive() {
  var path = "m/0'/0/0"
  var root = bitcoin.HDNode.fromSeedHex('dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd')
  var child1 = root.derivePath(path)
  var child1b = root.deriveHardened(0).derive(0).derive(0)
  console.log(child1.getAddress() == child1b.getAddress())

  var child2 = root.derivePath("m/44'/0'/0'/0/0")
  var child2b = root.deriveHardened(44).deriveHardened(0).deriveHardened(0).derive(0).derive(0)
  console.log(child1.getAddress() == child1b.getAddress())

  var root = bitcoin.HDNode.fromSeedHex('000102030405060708090a0b0c0d0e0f')
  console.log(root.neutered().toBase58() == 'xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8')
  console.log(root.toBase58() == 'xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi')

  var hdnode = root.derivePath("m/0'")
  console.log(hdnode.neutered().toBase58() == 'xpub68Gmy5EdvgibQVfPdqkBBCHxA5htiqg55crXYuXoQRKfDBFA1WEjWgP6LHhwBZeNK1VTsfTFUHCdrfp1bgwQ9xv5ski8PX9rL2dZXvgGDnw')
  console.log(hdnode.toBase58() == 'xprv9uHRZZhk6KAJC1avXpDAp4MDc3sQKNxDiPvvkX8Br5ngLNv1TxvUxt4cV1rGL5hj6KCesnDYUhd7oWgT11eZG7XnxHrnYeSvkzY7d2bhkJ7')

  hdnode = root.derivePath('m/0\'/1')
  console.log(hdnode.neutered().toBase58() == 'xpub6ASuArnXKPbfEwhqN6e3mwBcDTgzisQN1wXN9BJcM47sSikHjJf3UFHKkNAWbWMiGj7Wf5uMash7SyYq527Hqck2AxYysAA7xmALppuCkwQ')
  console.log(hdnode.toBase58() == 'xprv9wTYmMFdV23N2TdNG573QoEsfRrWKQgWeibmLntzniatZvR9BmLnvSxqu53Kw1UmYPxLgboyZQaXwTCg8MSY3H2EU4pWcQDnRnrVA1xe8fs')

  hdnode = root.derivePath('m/0\'/1/2\'')
  console.log(hdnode.neutered().toBase58() == 'xpub6D4BDPcP2GT577Vvch3R8wDkScZWzQzMMUm3PWbmWvVJrZwQY4VUNgqFJPMM3No2dFDFGTsxxpG5uJh7n7epu4trkrX7x7DogT5Uv6fcLW5')
  console.log(hdnode.toBase58() == 'xprv9z4pot5VBttmtdRTWfWQmoH1taj2axGVzFqSb8C9xaxKymcFzXBDptWmT7FwuEzG3ryjH4ktypQSAewRiNMjANTtpgP4mLTj34bhnZX7UiM')

  hdnode = root.derivePath('m/0\'/1/2\'/2')
  console.log(hdnode.neutered().toBase58() == 'xpub6FHa3pjLCk84BayeJxFW2SP4XRrFd1JYnxeLeU8EqN3vDfZmbqBqaGJAyiLjTAwm6ZLRQUMv1ZACTj37sR62cfN7fe5JnJ7dh8zL4fiyLHV')
  console.log(hdnode.toBase58() == 'xprvA2JDeKCSNNZky6uBCviVfJSKyQ1mDYahRjijr5idH2WwLsEd4Hsb2Tyh8RfQMuPh7f7RtyzTtdrbdqqsunu5Mm3wDvUAKRHSC34sJ7in334')

  hdnode = root.derivePath('m/0\'/1/2\'/2/1000000000')
  console.log(hdnode.neutered().toBase58() == 'xpub6H1LXWLaKsWFhvm6RVpEL9P4KfRZSW7abD2ttkWP3SSQvnyA8FSVqNTEcYFgJS2UaFcxupHiYkro49S8yGasTvXEYBVPamhGW6cFJodrTHy')
  console.log(hdnode.toBase58() == 'xprvA41z7zogVVwxVSgdKUHDy1SKmdb533PjDz7J6N6mV6uS3ze1ai8FHa8kmHScGpWmj4WggLyQjgPie1rFSruoUihUZREPSL39UNdE3BBDu76')
}

function wifAndSign() {
  var testnet = bitcoin.networks.testnet
  var tx = new bitcoin.TransactionBuilder(testnet)
  var bob = bitcoin.ECPair.fromWIF('cMkopUXKWsEzAjfa1zApksGRwjVpJRB3831qM9W4gKZsLwjHXA9x', testnet)
  tx.addInput('8a328a085fa0d6a192d7f3d468b457a60de98e9446df26b0fc825f21fde7cb60', 1)
  tx.addOutput('mixkyHmR9uJZgQYXwgFzAK4GGXkVuhzzJ9', 9000000)
  tx.sign(0, bob)
  console.log(tx.build().toHex() == '010000000160cbe7fd215f82fcb026df46948ee90da657b468d4f3d792a1d6a05f088a328a010000006a473044022018247362eddf75ac5e69c0edf9ed67153aee4716d687b89463c366f222dd6b3202202602205a463cfb491e8a31aa2eb2d25f9a49b37a5be23e90944b3c09063680bd0121038f0248cc0bebc425eb55af1689a59f88119c69430a860c6a05f340e445c417d7ffffffff0140548900000000001976a91425c9bd6dff69eb0321c09554ce8db2eb757c0bce88ac00000000')
}

function deriveAndSign() {
  var testnet = bitcoin.networks.testnet

  var root = bitcoin.HDNode.fromSeedHex('000102030405060708090a0b0c0d0e0f', testnet)
  var node = root.derivePath("m/44'/0'/0'/0/0")

  var tx = new bitcoin.TransactionBuilder(testnet)
  tx.addInput('8a328a085fa0d6a192d7f3d468b457a60de98e9446df26b0fc825f21fde7cb60', 1)
  tx.addOutput('mixkyHmR9uJZgQYXwgFzAK4GGXkVuhzzJ9', 9000000)
  tx.sign(0, node.keyPair)
  console.log(tx.build().toHex() == '010000000160cbe7fd215f82fcb026df46948ee90da657b468d4f3d792a1d6a05f088a328a010000006a473044022056796e3ad713972c6e0c5a6bdff9d3a1a7c99900c261ddbfdbc22b2b63301b8a0220009fce26c35f17cdc0b743ab48273d08da6a93499acb56954c06e10f906f02d101210239b4b3a27cd1dd8993038d5eb6449220b350c32ae62fec0833b93db8a49031c5ffffffff0140548900000000001976a91425c9bd6dff69eb0321c09554ce8db2eb757c0bce88ac00000000')
}

function bip39T() {
  var mnemonic = bip39.entropyToMnemonic('00000000000000000000000000000000')
  console.log(mnemonic == 'abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about')

  var entropy = bip39.mnemonicToEntropy(mnemonic)
  console.log(entropy == '00000000000000000000000000000000')
}

function bip38T() {
  var myWifString = '5KN7MzqK5wt2TP1fQCYyHBtDrXdJuXbUzm4A9rKAteGu3Qi5CVR'
  var decoded = wif.decode(myWifString)

  var encryptedKey = bip38.encrypt(decoded.privateKey, decoded.compressed, 'abcxyz')
  console.log(encryptedKey == '6PRVWUbkz4VdA3TPUdUwxgNcXRbdRbDZiLYcs9MDCm8eWeUvy2ctsKxZeC')

  var decryptedKey = bip38.decrypt(encryptedKey, 'abcxyz', function (status) {
    // console.log(status.percent) // will print the precent every time current increases by 1000
  })
  
  console.log(wif.encode(0x80, decryptedKey.privateKey, decryptedKey.compressed) == myWifString)
}

export const run = () => {
  console.debug("Test Start");
  genRandomAddress()
  derive()
  wifAndSign()
  deriveAndSign()
  bip39T()
  bip38T()
  console.debug("Test Done");
};