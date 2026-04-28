import { Vec2d, Vec2dSimple, VecLike } from "./Vec2d";
/**
 * A serializable model for 2D vectors.
 *
 * @public */
export interface Point2dModel extends Vec2dSimple {
    /** 压力值 */
    z: number;
    /** 基于上一点的向量 */
    v?: VecLike;
    /** 时间戳 */
    t?: number;
    /** 当前点基于前后两个点的夹角度数(0-360) */
    a?: number;
}
/** @public */
export type PointLike = Point2d | Point2dModel;
/** @public */
export declare class Point2d extends Vec2d {
    x: number;
    y: number;
    z: number;
    v: {
        x: number;
        y: number;
    };
    t: number;
    a: number;
    constructor(x?: number, y?: number, z?: number, v?: {
        x: number;
        y: number;
    }, t?: number, a?: number);
    get timestamp(): number;
    get pressure(): number;
    get angleNum(): number;
    get XY(): [number, number];
    setA(a: number): void;
    setT(t: number): void;
    setv(v: Vec2dSimple): this;
    set(x?: number, y?: number, z?: number, v?: {
        x: number;
        y: number;
    }, t?: number, a?: number): this;
    clone(): Point2d;
    distance(p: Point2d): number;
    isNear(p: Point2d, tolerance: number): boolean;
    getAngleByPoints(pre: Point2d, next: Point2d): number;
    static Sub(A: PointLike, B: VecLike): Point2d;
    static Add(A: PointLike, B: VecLike): Point2d;
    static GetDistance(p0: Point2d, p1: Point2d): number;
    static GetAngleByPoints(pre: Point2d, cur: Point2d, next: Point2d): number;
    static IsNear(p0: Point2d, p1: Point2d, tolerance: number): boolean;
    static RotWith(A: Point2d | Vec2d, C: Point2d | Vec2d, r: number, decimal?: number): Point2d;
    /**
     * 根据圆心和半径，获取圆上的等份点
     * @param o 圆心
     * @param radius 半径
     * @param average 均分数
     * @returns
     */
    static GetDotStroke(o: Point2d, radius: number, average?: number): Point2d[];
    /**
     * 根据圆心和圆上的起始点，获取半圆上的等份点
     * @param o 圆心
     * @param p 圆弧起始点
     * @param radian 1，逆时针180度 -1，顺时针
     * @param average 均分数
     * @returns
     */
    static GetSemicircleStroke(o: Point2d, p: Point2d, radian?: number, average?: number): Point2d[];
}
