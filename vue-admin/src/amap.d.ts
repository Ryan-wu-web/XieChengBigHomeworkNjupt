declare global {
  interface Window {
    AMap: {
      Map: new (containerId: string | HTMLElement, options: any) => any
      Marker: new (options: any) => any
      PlaceSearch: new (options: any) => any
      Geocoder: new (options: any) => any
      Event: {
        addListener: (target: any, eventName: string, handler: Function) => void
        removeListener: (listener: any) => void
      }
    }
  }

  const AMap: Window['AMap']
}

export {}
