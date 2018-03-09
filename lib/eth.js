const HDKey = require('ethereumjs-wallet/hdkey.js')
const EthereumTx = require('ethereumjs-tx')
const RLP = require('rlp')
function derive() {
  var root = HDKey.fromExtendedKey('xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8')
  var hdnode = root.derivePath('m/0/0')
  console.log(hdnode.publicExtendedKey() == 'xpub6AvUGrnEpfvJ8L7GLRkBTByQ9uBvUHp9o5VxHrFxhvzV4dSWkySpNaBoLR9FpbnwRmTa69yLHF3QfcaxbWT7gWdwws5k4dpmJvqpEuMWwnj')
  console.log(hdnode.getWallet().getAddressString() == '0x4b7115ad9623a528f1845eaf85d166de1e869bfb')
  hdnode = root.derivePath('m/0/1')
  console.log(hdnode.publicExtendedKey() == 'xpub6AvUGrnEpfvJBbfx7sQ89Q8hEMPM65UteqEX4yUbUiES2jHfjexmfJoxCGSwFMZiPBaKQT1RiKWrKfuDV4vpgVs4Xn8PpPTR2i79rwHd4Zr')
  hdnode = root.derivePath('m/0/2')
  console.log(hdnode.publicExtendedKey() == 'xpub6AvUGrnEpfvJFYHymqh5qJ3V7qFyEFdpQom2tRQdV4Eo25kxagwHwVCMX1opKqAXxacHPAJafQW1uvH3bYQi1zbE5DMgXGAGNkHajLEuoa2')
  hdnode = root.derivePath('m/0/3')
  console.log(hdnode.publicExtendedKey() == 'xpub6AvUGrnEpfvJJFCTd6qEYfMaxryBU8BykimDwQYuJJawFEh9BiyFdr37Cc4wEKCWWv7TsFQRUMdezXVqV9cfBUbeUEgNYCCP4omxULbNaRr')
  hdnode = root.derivePath('m/0/4')
  console.log(hdnode.publicExtendedKey() == 'xpub6AvUGrnEpfvJK8JSEZgaj8KdpyJdkPmvEijJAEDonznEfggUqcvS79ZD56u6fnuPUUgRXTicNMuhcG7sfWoFLCzgNCoq93JxrHTxVLSabdq')
  hdnode = root.derivePath('m/0/4/0/21')
  console.log(hdnode.publicExtendedKey() == 'xpub6EZymfsyrZKjfNtPsVxDQcAsdqEyh6BzrvoELTKrds3ofR1i8RaFdsppN2UgV3B2qZrc9bA2VRtmoNxgp9mTsF3eB5b1ZJasKJXQw6jgRsy')
  root = HDKey.fromExtendedKey('xpub6AvUGrnEpfvJK8JSEZgaj8KdpyJdkPmvEijJAEDonznEfggUqcvS79ZD56u6fnuPUUgRXTicNMuhcG7sfWoFLCzgNCoq93JxrHTxVLSabdq')
  hdnode = root.derivePath('0/21')
  console.log(hdnode.publicExtendedKey() == 'xpub6CwhnQCgD1xVA1MKn7zPzeUHHpGitxwqiEwebCNrjyBiTxXxQV9yhvNKcUBNE5t8GcLe5ovipGvWhqadbTbWFktwbmohis9CqEizRG7miQ1')
}
function tx() {
  const privateKey = Buffer.from('e331b6d69882b4cb4ea581d88e0b604039a3de5967688d3dcffdd2270c0fd109', 'hex')
  const txParams = {
    nonce: '0x00',
    gasPrice: '0x09184e72a000', 
    gasLimit: '0x2710',
    to: '0x0000000000000000000000000000000000000000', 
    value: '0x00', 
    data: '0x7f7465737432000000000000000000000000000000000000000000000000000000600057',
    // EIP 155 chainId - mainnet: 1, ropsten: 3
    chainId: 3
  }
  const tx = new EthereumTx(txParams)
  tx.sign(privateKey)
  const t = tx.serialize().toString('hex')
  console.log(t == 'f889808609184e72a00082271094000000000000000000000000000000000000000080a47f746573743200000000000000000000000000000000000000000000000000000060005729a0f2d54d3399c9bcd3ac3482a5ffaeddfe68e9a805375f626b4f2f8cf530c2d95aa05b3bb54e6e8db52083a9b674e578c843a87c292f0383ddba168573808d36dc8e')
}
function rlp() {
  var encodedSelf = RLP.encode('a')
  console.log(encodedSelf.toString() == 'a')
  var encodedDog = RLP.encode('dog')
  console.log(encodedDog.length == 4)
  console.log(encodedDog[0] == 131)
  console.log(encodedDog[1] == 100)
  console.log(encodedDog[2] == 111)
  console.log(encodedDog[3] == 103)
  var t = {
    to: "0x56D193dfF4a250c6F09dEd7FE364Afe35bba337F",
    value: "0x1000",
    nonce: "0x0",
    gasPrice: "0x04a817c800",
    gasLimit: 21000,
    data: "",
    r: "0x9c21304b3c79f37351a33f7f68643c4f7a5f4ce22450f99a8b7e55dc6c17d107",
    s: "0x1dda0ebcec7c1b9876b5e95f2ea5e020f5f15945bcd6df445b6f977b42d675a3",
    v: "0x1b"
  }
  var encoded = RLP.encode([t.nonce, t.gasPrice, t.gasLimit, t.to, t.value, t.data, t.v, t.r, t.s])
  console.log(encoded.toString('hex') == 'f866008504a817c8008252089456d193dff4a250c6f09ded7fe364afe35bba337f821000801ba09c21304b3c79f37351a33f7f68643c4f7a5f4ce22450f99a8b7e55dc6c17d107a01dda0ebcec7c1b9876b5e95f2ea5e020f5f15945bcd6df445b6f977b42d675a3')
}

export const ethTest = () => {
  console.info("Eth test start");
  derive()
  tx()
  rlp()
  console.info("Eth test done");
};