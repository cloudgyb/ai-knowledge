import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useInputMsgStore = defineStore('userInputMsg', () => {
    const inputMsg = ref<string>('')
    const kbId = ref<number>(NaN)

    function setInputMsg(msg: string) {
        inputMsg.value = msg
    }

    function clear() {
        inputMsg.value = ''
        kbId.value = NaN
    }

    function setKbId(id: number) {
        kbId.value = id
    }

    return {
        inputMsg,
        kbId,
        setKbId,
        setInputMsg,
        clear
    }
})
