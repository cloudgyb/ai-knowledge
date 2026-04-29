/// <reference types="vite/client" />

// 环境变量声明
declare module '*.vue' {
    import type {DefineComponent} from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}

// Vite 环境变量
interface ImportMetaEnv {
    readonly VITE_APP_TITLE: string
    readonly VITE_API_BASE_URL: string
    readonly VITE_APP_VERSION: string
    readonly VITE_APP_ENV: 'development' | 'production' | 'test'
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}

// htmlparser2 types
declare module 'domhandler' {
    export interface Node {
        type: string
        data?: string
        children?: Node[]
        attribs?: Record<string, string>
    }

    export interface Text extends Node {
        type: 'text'
        data: string
    }

    export interface Element extends Node {
        type: 'tag'
        name: string
        tagName: string
        attribs?: Record<string, string>
        children?: Node[]
    }
}

interface Window {
    copyToClipboard: (text: string) => Promise<void>
}

