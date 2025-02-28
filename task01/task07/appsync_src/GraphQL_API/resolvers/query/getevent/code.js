import { util } from '@aws-appsync/utils';

/**
 * Sends a request to the attached data source
 * @param {import('@aws-appsync/utils').Context} ctx the context
 * @returns {*} the request
 */
export function request(ctx) {
    // Update with custom logic or select a code sample.
    return {
        operation: "Query",
        query: {
            expression: "id = :id",
            expressionValues: util.dynamodb.toMapValues({
                ":id": ctx.args.id
            })
        }
    };
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
    if (!ctx.result || !ctx.result.items || ctx.result.items.length === 0) {
        return util.error("Event not found.", "NotFoundError");
    }

    return ctx.result.items[0];
}
