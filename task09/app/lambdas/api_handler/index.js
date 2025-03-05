const axios = require('axios');

class OpenMeteoService {
    constructor() {
        this.apiEndpoint = 'https://api.open-meteo.com/v1/forecast';
    }

    async fetchWeatherData(lat, lon) {
        try {
            const result = await axios.get(this.apiEndpoint, {
                params: {
                    latitude: lat,
                    longitude: lon,
                    hourly: ['temperature_2m', 'relative_humidity_2m', 'wind_speed_10m'],
                    current: ['temperature_2m', 'wind_speed_10m'],
                    timezone: 'auto'
                }
            });
            return result.data;
        } catch (err) {
            console.error('Weather data retrieval error:', err.message);
            throw new Error('Unable to retrieve weather details');
        }
    }
}

exports.handler = async (event) => {
    const requestPath = event?.requestContext?.http?.path;
    const requestMethod = event?.requestContext?.http?.method;

    if (requestPath !== "/weather" || requestMethod !== "GET") {
        return {
            statusCode: 400,
            body: JSON.stringify({
                statusCode: 400,
                message: `Bad request syntax or unsupported method. Request path: ${requestPath}. HTTP method: ${requestMethod}`
            })
        };        
    }

    try {
        const lat = 50.4375;
        const lon = 30.5;
        const weatherService = new OpenMeteoService();
        const weatherDetails = await weatherService.fetchWeatherData(lat, lon);

        return {
            statusCode: 200,
            body: JSON.stringify(weatherDetails)
        };
    } catch (err) {
        return {
            statusCode: 500,
            body: JSON.stringify({ message: "Server encountered an issue" })
        };
    }
};
