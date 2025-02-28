import { util } from '@aws-appsync/utils';
import * as ddb from '@aws-appsync/utils/dynamodb';
/**
 * Sends a request to the attached data source
 * @param {import('@aws-appsync/utils').Context} ctx the context
 * @returns {*} the request
 */
export function request(ctx) {
    // Update with custom logic or select a code sample.
    const id = util.autoId(); // Generate a UUID
    const createdAt = util.time.nowISO8601(); // Get current timestamp

    return ddb.put({
        key: { id },
        item: {
            id,
            userId: ctx.args.userId,
            createdAt,
            payLoad: ctx.args.payLoad
        }
    });
}

/**
 * Returns the resolver result
 * @param {import('@aws-appsync/utils').Context} ctx the context
 * @returns {*} the result
 */
export function response(ctx) {
    // Update with response logic
    if (ctx.error) {
        return util.error(ctx.error.message, ctx.error.type);
    }
    return ctx.result;
}
