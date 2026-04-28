import JSEncrypt from 'jsencrypt'
import {modelApi} from "@/api/model";


const publicKey = await modelApi.getPublicKey()
console.log(publicKey)

export function rsaEncrypt(data: string) {
    let encryptor = new JSEncrypt()  // 创建加密对象实例
    //之前生成的公钥，复制的时候要小心不要有空格(此处把密钥省略了，自己写的时候可把自己生成的公钥粘到对应位置)
    encryptor.setPublicKey(publicKey.data)//设置公钥
    const encrypt = encryptor.encrypt(data);
    if (encrypt === false) {
        throw new Error('加密失败')
    }
    return encrypt
}


export function rsaDecrypt(data: string): string {
    let decryptor = new JSEncrypt()  // 创建加密对象实例
    decryptor.setPublicKey(publicKey.data)//设置私钥
    const decrypt = decryptor.decrypt(data);
    if (decrypt === false) {
        throw new Error('解密失败')
    }
    return decrypt
}